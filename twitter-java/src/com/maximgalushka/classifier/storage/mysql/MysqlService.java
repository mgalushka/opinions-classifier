package com.maximgalushka.classifier.storage.mysql;

import com.google.gson.Gson;
import com.maximgalushka.classifier.clustering.model.TweetClass;
import com.maximgalushka.classifier.twitter.account.TwitterAccount;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.model.ScheduledTweet;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.carrot2.core.Cluster;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * @since 9/16/2014.
 */
@SuppressWarnings("UnusedDeclaration")
public class MysqlService {

  public static final Logger log = Logger.getLogger(MysqlService.class);

  private DataSource datasource;
  private Gson gson = new Gson();

  public MysqlService() throws SQLException {
  }

  public void setDatasource(DataSource datasource) {
    this.datasource = datasource;
  }

  /**
   * @return clusters persisted in database to store them to cache
   */
  public HashMap<Long, Clusters> loadClusters(long period) {
    final HashMap<Long, Clusters> result = new HashMap<>();
    final long now = new Date().getTime();
    final long diff = now - period;
    return query(
      String.format(
        "select timestamp, clusters_serialized from clusters " +
          "where timestamp > %d", diff
      ),
      set -> {
        try {
          while (set.next()) {
            long timestamp = set.getLong(1);
            Blob data = set.getBlob(2);
            ObjectInputStream in = new ObjectInputStream(
              data.getBinaryStream()
            );
            Clusters clusters = (Clusters) in.readObject();
            result.put(timestamp, clusters);
          }
        } catch (ClassNotFoundException | IOException | SQLException e) {
          log.error(e);
          e.printStackTrace();
        }
        return result;
      }
    );
  }

  /**
   * @return clusters persisted in database to store them to cache
   */
  public HashMap<Long, Clusters> loadLastClusters(long count) {
    final HashMap<Long, Clusters> result = new HashMap<>();
    return query(
      String.format(
        "select timestamp, clusters_serialized from clusters " +
          "order by timestamp desc limit %d", count
      ),
      set -> {
        try {
          while (set.next()) {
            long timestamp = set.getLong(1);
            Blob data = set.getBlob(2);
            ObjectInputStream in = new ObjectInputStream(
              data.getBinaryStream()
            );
            Clusters clusters = (Clusters) in.readObject();
            result.put(timestamp, clusters);
          }
        } catch (ClassNotFoundException | IOException | SQLException e) {
          log.error(e);
          e.printStackTrace();
        }
        return result;
      }
    );
  }

  public void saveNewClustersGroup(long timestamp, Clusters group) {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      conn = this.datasource.getConnection();
      stmt = conn.prepareStatement(
        "insert into clusters (timestamp, clusters_serialized) values (?, ?)"
      );
      stmt.setLong(1, timestamp);

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bos);
      out.writeObject(group);
      byte[] encoded = bos.toByteArray();

      stmt.setBlob(2, new ByteArrayInputStream(encoded));
      stmt.executeUpdate();
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (Exception e) {
        log.error(e);
        e.printStackTrace();
      }
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        log.error(e);
        e.printStackTrace();
      }
    }
  }

  public List<TwitterAccount> getActiveAccounts() {
    return accountsQuery(
      "select id, account, consumer_key, consumer_secret, " +
        "access_token, access_token_secret, terms, lang, term_black_list, " +
        "users_black_list, user_access_token, user_access_token_secret " +
        "from accounts " +
        "where is_active = 1"
    );
  }

  public TwitterAccount getAccountById(long accountId) {
    List<TwitterAccount> wrapped = accountsQuery(
      String.format(
        "select id, account, consumer_key, consumer_secret, " +
          "access_token, access_token_secret, terms, lang, term_black_list, " +
          "users_black_list, user_access_token, user_access_token_secret " +
          "from accounts " +
          "where id = %d AND is_active = 1",
        accountId
      )
    );
    if (wrapped == null || wrapped.isEmpty()) {
      return null;
    } else {
      return wrapped.get(0);
    }
  }

  private List<TwitterAccount> accountsQuery(String query) {
    return query(
      query,
      set -> {
        List<TwitterAccount> accounts = new ArrayList<>();
        try {
          while (set.next()) {
            TwitterAccount account = new TwitterAccount();
            account.setId(set.getLong("id"));
            account.setAccount(set.getString("account"));
            account.setConsumerKey(set.getString("consumer_key"));
            account.setConsumerSecret(set.getString("consumer_secret"));
            account.setAccessToken(set.getString("access_token"));
            account.setAccessTokenSecret(set.getString("access_token_secret"));
            account.setLanguage(set.getString("lang"));
            account.setTerms(set.getString("terms"));
            account.setBlacklist(set.getString("term_black_list"));
            account.setUsersBlacklist(set.getString("users_black_list"));
            account.setUserAccessToken(set.getString("user_access_token"));
            account.setUserAccessTokenSecret(
              set.getString(
                "user_access_token_secret"
              )
            );
            accounts.add(account);
          }
        } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        return accounts;
      }
    );
  }

  public void updateUserAccountToken(
    long accountId,
    String userAccessToken,
    String userAccessTokenSecret
  ) {
    try (Connection conn = this.datasource.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(
        "update accounts " +
          "set user_access_token = ?, " +
          "user_access_token_secret = ? " +
          "where id = ?"
      );
      stmt.setString(1, userAccessToken);
      stmt.setString(2, userAccessTokenSecret);
      stmt.setLong(3, accountId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void saveTweetsBatch(Collection<Tweet> tweets) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into tweets_all" +
            "(id, account_id, content_json, tweet_cleaned, created_timestamp)" +
            "values (?, ?, ?, ?, now())"
        )
      ) {
        for (Tweet tweet : tweets) {
          stmt.setLong(1, tweet.getId());
          stmt.setLong(2, tweet.getAccountId());
          stmt.setString(3, gson.toJson(tweet));
          stmt.setString(4, tweet.getText());
          stmt.addBatch();
        }
        stmt.executeBatch();
      } catch (BatchUpdateException e) {
        if (e.getMessage().contains("Duplicate entry")) {
          log.trace(
            String.format(
              "Ignoring constraint violation exception: %s",
              e
            )
          );
        } else {
          throw e;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Tweet getTweetById(long tweetId) {
    return query(
      String.format(
        "select id, account_id, content_json, tweet_cleaned, " +
          "created_timestamp " +
          "from tweets_all " +
          "where id = %d ",
        tweetId
      ),
      set -> {
        Tweet tweet = null;
        try {
          if (set.next()) {
            tweet = gson.fromJson(set.getString(3), Tweet.class);
            tweet.setAccountId(set.getLong(2));
          }
        } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        return tweet;
      }
    );
  }

  public void updateTweetClass(Tweet tweet, TweetClass clazz) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "update tweets_all " +
            "set classified = ? " +
            "where id = ? AND classified IS NULL"
        )
      ) {
        stmt.setString(1, clazz.getClazz());
        stmt.setLong(2, tweet.getId());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void scheduleTweet(
    long accountId,
    Tweet tweet,
    Tweet original,
    boolean retweet
  ) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into tweets_scheduled " +
            "(id, account_id, text, original_json, retweet, scheduled, " +
            "published, created_timestamp) " +
            "values (?, ?, ?, ?, ?, 0, 0, now())"
        )
      ) {
        stmt.setLong(1, tweet.getId());
        stmt.setLong(2, accountId);
        stmt.setString(3, tweet.getText());
        stmt.setString(4, gson.toJson(original));
        stmt.setInt(5, retweet ? 1 : 0);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void unpublishTweetCluster(long tweetId) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "update tweets_clusters " +
            "set is_displayed = 0 " +
            "where best_tweet_id = ? "
        )
      ) {
        stmt.setLong(1, tweetId);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updateScheduled(long id, Date scheduled) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "update tweets_scheduled " +
            "set scheduled = 1, " +
            "scheduled_timestamp = ? " +
            "where id = ? "
        )
      ) {
        stmt.setTimestamp(1, new java.sql.Timestamp(scheduled.getTime()));
        stmt.setLong(2, id);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updatePublished(long id, long publishedId, boolean success) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "update tweets_scheduled " +
            "set published = 1, " +
            "published_id = ?, " +
            "status = ?, " +
            "published_timestamp = now() " +
            "where id = ? "
        )
      ) {
        stmt.setLong(1, publishedId);
        stmt.setInt(2, success ? 1 : 0);
        stmt.setLong(3, id);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List<ScheduledTweet> getUnscheduledTweets(long accountId) {
    return scheduledQuery(
      String.format(
        "select id, account_id, text, original_json, retweet, " +
          "scheduled_timestamp " +
          "from tweets_scheduled " +
          "where account_id = %d AND scheduled = 0",
        accountId
      )
    );
  }

  public List<ScheduledTweet> getScheduledUnpublishedTweets(long accountId) {
    return scheduledQuery(
      String.format(
        "select id, account_id, text, original_json, retweet, " +
          "scheduled_timestamp " +
          "from tweets_scheduled " +
          "where account_id = %d AND scheduled = 1 AND published = 0",
        accountId
      )
    );
  }

  private List<ScheduledTweet> scheduledQuery(String query) {
    return query(
      query,
      set -> {
        List<ScheduledTweet> unscheduled = new ArrayList<>();
        try {
          while (set.next()) {
            long id = set.getLong(1);
            long accountId = set.getLong(2);
            String text = set.getString(3);
            Tweet original = gson.fromJson(set.getString(4), Tweet.class);
            original.setAccountId(accountId);
            boolean retweet = (set.getInt(5) == 1);
            if (!retweet) {
              original.setText(text);
            }
            ScheduledTweet scheduled = new ScheduledTweet(original, retweet);
            scheduled.setScheduled(set.getTimestamp(6));
            unscheduled.add(scheduled);
          }
        } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        return unscheduled;
      }
    );
  }

  public Date getLatestPublishedOrScheduledTimestamp(
    long accountId,
    boolean retweet
  ) {
    return query(
      String.format(
        "select " +
          "  greatest(" +
          "   now(), " +
          "   coalesce(max(scheduled_timestamp), now())) as latest " +
          "  from " +
          "    tweets_scheduled " +
          "  where account_id = %d AND " +
          "(published = 1 or scheduled = 1) AND retweet = %d",
        accountId, retweet ? 1 : 0
      ),
      set -> {
        Date latest = new Date();
        try {
          if (set.next()) {
            latest = set.getTimestamp(1);
          }
        } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        return latest;
      }
    );
  }


  public Long getMaxRunId(long accountId)
  throws Exception {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          String.format(
            "select coalesce(max(cluster_run_id), 0) as max_id " +
              "from tweets_clusters where account_id = %d",
            accountId
          )
        )
      ) {
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          return rs.getLong(1);
        } else {
          throw new Exception("Cannot calculate max cluster_run_id");
        }
      }
    }
  }

  public Long createNewCluster(
    long accountId,
    Cluster cluster,
    long clusterRunId
  )
  throws Exception {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into tweets_clusters" +
            "(name, account_id, cluster_run_id, cluster_run_timestamp, " +
            "created_timestamp, updated_timestamp) " +
            "values (?, ?, ?, now(), now(), now())",
          Statement.RETURN_GENERATED_KEYS
        )
      ) {
        stmt.setString(1, cluster.getLabel());
        stmt.setLong(2, accountId);
        stmt.setLong(3, clusterRunId);
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
          return rs.getLong(1);
        } else {
          throw new Exception("Cannot retrieve created cluster id");
        }
      }
    }
  }

  public void saveTweetsClustersBatch(
    long runId,
    long accountId,
    long newClusterId,
    List<Tweet> tweetsInCluster
  ) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into clusters_runs (" +
            "run_id, account_id, tweet_id, cluster_id) " +
            "values (?, ?, ?, ?)"
        )
      ) {
        for (Tweet tweet : tweetsInCluster) {
          stmt.setLong(1, runId);
          stmt.setLong(2, accountId);
          stmt.setLong(3, tweet.getId());
          stmt.setLong(4, newClusterId);
          stmt.addBatch();
        }
        stmt.executeBatch();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void saveTweetsCleanedBatch(
    Collection<Tweet> tweets
  ) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "update tweets_all " +
            "set tweet_cleaned=?, " +
            "excluded=?, " +
            "excluded_reason=? " +
            "where id=?"
        )
      ) {
        int BATCH_SIZE = 100;
        int counter = 0;
        for (Tweet tweet : tweets) {
          stmt.setString(1, tweet.getText());
          stmt.setInt(2, tweet.isExcluded() ? 1 : 0);
          String reason = tweet.getExcludedReason();
          String truncatedReason = reason.substring(
            0, Math.min(reason.length(), 256)
          );
          stmt.setString(3, truncatedReason);
          stmt.setLong(4, tweet.getId());
          stmt.addBatch();
          counter++;
          if (counter % BATCH_SIZE == 0) {
            log.trace(
              String.format(
                "Saving batch number [%d]",
                counter / BATCH_SIZE
              )
            );
            stmt.executeBatch();
          }
        }
        log.debug("Saved all batches with cleaned tweets.");
        stmt.executeBatch();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updateTweetsFeaturesBatch(
    Map<Tweet, Map<String, Object>> features
  ) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "update tweets_all " +
            "set features=? " +
            "where id=?"
        )
      ) {
        int BATCH_SIZE = 100;
        int counter = 0;
        for (Map.Entry<Tweet, Map<String, Object>> feature : features
          .entrySet()) {
          stmt.setString(1, gson.toJson(feature.getValue()));
          stmt.setLong(2, feature.getKey().getId());
          stmt.addBatch();
          counter++;
          if (counter % BATCH_SIZE == 0) {
            log.trace(
              String.format(
                "Saving features batch number [%d]",
                counter / BATCH_SIZE
              )
            );
            stmt.executeBatch();
          }
        }
        stmt.executeBatch();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void saveBestTweetInCluster(long clusterId, long tweetId) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "update tweets_clusters " +
            "set best_tweet_id=? " +
            "where cluster_id=?"
        )
      ) {
        stmt.setLong(1, tweetId);
        stmt.setLong(2, clusterId);
        stmt.execute();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void saveTweetLabel(Tweet tweet, String label) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "update tweets_all " +
            "set label=? " +
            "where id=?"
        )
      ) {
        stmt.setString(1, label);
        stmt.setLong(2, tweet.getId());
        stmt.execute();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Latest tweets for latest frame in hours
   *
   * @param hours hours to find tweets from, no more then 5000 tweets.
   */
  public List<Tweet> getLatestTweets(long accountId, double hours) {
    return query(
      String.format(
        "select id, content_json, tweet_cleaned, " +
          "created_timestamp " +
          "from tweets_all " +
          "where account_id = %d AND " +
          "created_timestamp >= DATE_SUB(NOW(), INTERVAL %f HOUR) " +
          "LIMIT 2000", accountId, hours
      ),
      set -> {
        List<Tweet> tweets = new ArrayList<>();
        try {
          while (set.next()) {
            Tweet tweet = gson.fromJson(set.getString(2), Tweet.class);
            tweet.setAccountId(accountId);
            tweets.add(tweet);
          }
        } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        return tweets;
      }
    );
  }

  public List<Tweet> getLatestUsedTweets(
    long accountId,
    double hours,
    boolean published,
    boolean rejected
  ) {
    if (!published || !rejected) {
      throw new IllegalArgumentException(
        "Currently only both published and rejected are supported as return"
      );
    }
    return query(
      String.format(
        "select t.id, t.content_json " +
          "from tweets_clusters c join tweets_all t " +
          "on c.best_tweet_id = t.id " +
          "where c.account_id = %d AND " +
          "c.is_displayed = 0 AND " +
          "c.created_timestamp > DATE_SUB(NOW(), INTERVAL %f HOUR) " +
          "union all " +
          "select id, original_json " +
          "from tweets_scheduled t " +
          "where account_id = %d AND " +
          "published_timestamp > DATE_SUB(NOW(), INTERVAL %f HOUR)",
        accountId, hours, accountId, hours
      ),
      set -> {
        List<Tweet> tweets = new ArrayList<>();
        try {
          while (set.next()) {
            Tweet tweet = gson.fromJson(set.getString(2), Tweet.class);
            tweet.setExcluded(true);
            tweet.setAccountId(accountId);
            tweets.add(tweet);
          }
        } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        return tweets;
      }
    );
  }

  public List<Tweet> getTweetsForRun(long accountId, long runId) {
    return query(
      String.format(
        "select t.id, t.content_json, t.tweet_cleaned, " +
          "t.created_timestamp " +
          "from tweets_all t join clusters_runs c " +
          "on t.id = c.tweet_id " +
          "where c.account_id = %d AND c.run_id = %d", accountId, runId
      ),
      set -> {
        List<Tweet> tweets = new ArrayList<>();
        try {
          while (set.next()) {
            Tweet tweet = gson.fromJson(set.getString(2), Tweet.class);
            tweet.setAccountId(accountId);
            tweets.add(tweet);
          }
        } catch (SQLException e) {
          log.error(e);
          e.printStackTrace();
        }
        return tweets;
      }
    );
  }

  public List<Tweet> getBestTweetsForRun(long accountId, long runId) {
    return query(
      String.format(
        "select t.id, t.content_json, t.tweet_cleaned, " +
          "t.created_timestamp " +
          "from tweets_clusters c join tweets_all t " +
          "on c.best_tweet_id = t.id AND c.account_id = t.account_id " +
          "where c.account_id = %d AND c.cluster_run_id = %d", accountId, runId
      ),
      set -> {
        List<Tweet> tweets = new ArrayList<>();
        try {
          while (set.next()) {
            Tweet tweet = gson.fromJson(set.getString(2), Tweet.class);
            tweet.setAccountId(accountId);
            tweets.add(tweet);
          }
        } catch (SQLException e) {
          log.error(e);
          e.printStackTrace();
        }
        return tweets;
      }
    );
  }


  private <T> T query(String sql, Command<T> callback) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rset = null;

    try {
      conn = this.datasource.getConnection();
      stmt = conn.prepareStatement(sql);
      rset = stmt.executeQuery();
      return callback.process(rset);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (rset != null) {
          rset.close();
        }
      } catch (Exception e) {
        log.error(e);
        e.printStackTrace();
      }
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (Exception e) {
        log.error(e);
        e.printStackTrace();
      }
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        log.error(e);
        e.printStackTrace();
      }
    }
    return null;
  }
}
