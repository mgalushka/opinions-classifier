package com.maximgalushka.classifier.storage.mysql;

import com.google.gson.Gson;
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

  public void saveTweetsBatch(Collection<Tweet> tweets) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into tweets_all" +
            "(id, content_json, tweet_cleaned, created_timestamp)" +
            "values (?, ?, ?, now())"
        )
      ) {
        for (Tweet tweet : tweets) {
          stmt.setLong(1, tweet.getId());
          stmt.setString(2, gson.toJson(tweet));
          stmt.setString(3, tweet.getText());
          stmt.addBatch();
        }
        stmt.executeBatch();
      } catch (BatchUpdateException e) {
        if (e.getMessage().contains("Duplicate entry")) {
          log.trace(
            String.format("Ignoring constraint violation exception"),
            e
          );
        } else {
          throw e;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void savePublishedTweet(Tweet tweet, boolean retweet) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into tweets_published " +
            "select " +
            "id, " +
            "content_json as original_json, " +
            "? as published, " +
            "? as retweet, " +
            "now() as published_timestamp " +
            "from tweets_all " +
            "where id=?"
        )
      ) {
        stmt.setString(1, tweet.getText());
        stmt.setInt(2, retweet ? 1 : 0);
        stmt.setLong(3, tweet.getId());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Tweet getTweetById(long tweetId) {
    return query(
      String.format(
        "select id, content_json, tweet_cleaned, " +
          "created_timestamp " +
          "from tweets_all " +
          "where id = %d ",
        tweetId
      ),
      set -> {
        Tweet tweet = null;
        try {
          if (set.next()) {
            tweet = gson.fromJson(set.getString(2), Tweet.class);
          }
        } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        return tweet;
      }
    );
  }

  public void scheduleTweet(
    Tweet tweet,
    Tweet original,
    boolean retweet
  ) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into tweets_scheduled " +
            "(id, text, original_json, retweet, scheduled, published, " +
            "created_timestamp) " +
            "values (?, ?, ?, ?, 0, 0, now())"
        )
      ) {
        stmt.setLong(1, tweet.getId());
        stmt.setString(2, tweet.getText());
        stmt.setString(3, gson.toJson(original));
        stmt.setInt(4, retweet ? 1 : 0);
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

  public List<ScheduledTweet> getUnscheduledTweets() {
    return scheduledQuery(
      "select id, text, original_json, retweet, scheduled_timestamp " +
        "from tweets_scheduled " +
        "where scheduled = 0"
    );
  }

  public List<ScheduledTweet> getScheduledUnpublishedTweets() {
    return scheduledQuery(
      "select id, text, original_json, retweet, scheduled_timestamp " +
        "from tweets_scheduled " +
        "where scheduled = 1 and published = 0"
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
            String text = set.getString(2);
            Tweet original = gson.fromJson(set.getString(3), Tweet.class);
            boolean retweet = (set.getInt(4) == 1);
            if (!retweet) {
              original.setText(text);
            }
            ScheduledTweet scheduled = new ScheduledTweet(original, retweet);
            scheduled.setScheduled(set.getTimestamp(5));
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

  public Date getLatestPublishedOrScheduledTimestamp(boolean retweet) {
    return query(
      String.format(
        "select " +
          "  greatest(" +
          "   now(), " +
          "   coalesce(max(scheduled_timestamp), now())) as latest " +
          "  from " +
          "    tweets_scheduled " +
          "  where (published = 1 or scheduled = 1) and retweet = %d",
        retweet ? 1 : 0
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


  public Long getMaxRunId()
  throws Exception {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "select coalesce(max(cluster_run_id), 0) as max_id " +
            "from tweets_clusters"
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

  public Long createNewCluster(Cluster cluster, long clusterRunId)
  throws Exception {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into tweets_clusters" +
            "(name, cluster_run_id, cluster_run_timestamp, " +
            "created_timestamp, updated_timestamp) " +
            "values (?, ?, now(), now(), now())",
          Statement.RETURN_GENERATED_KEYS
        )
      ) {
        stmt.setString(1, cluster.getLabel());
        stmt.setLong(2, clusterRunId);
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
    long newClusterId,
    List<Tweet> tweetsInCluster
  ) {
    try (Connection conn = this.datasource.getConnection()) {
      try (
        PreparedStatement stmt = conn.prepareStatement(
          "insert into clusters_runs (run_id, tweet_id, cluster_id) " +
            "values (?, ?, ?)"
        )
      ) {
        for (Tweet tweet : tweetsInCluster) {
          stmt.setLong(1, runId);
          stmt.setLong(2, tweet.getId());
          stmt.setLong(3, newClusterId);
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
          stmt.setString(3, tweet.getExcludedReason());
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

  /**
   * Latest tweets for latest frame in hours
   *
   * @param hours hours to find tweets from, no more then 5000 tweets.
   */
  public List<Tweet> getLatestTweets(double hours) {
    return query(
      String.format(
        "select id, content_json, tweet_cleaned, " +
          "created_timestamp " +
          "from tweets_all " +
          "where created_timestamp >= DATE_SUB(NOW(), INTERVAL %f HOUR) " +
          "LIMIT 2000", hours
      ),
      set -> {
        List<Tweet> tweets = new ArrayList<>();
        try {
          while (set.next()) {
            tweets.add(gson.fromJson(set.getString(2), Tweet.class));
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
    double hours,
    boolean published,
    boolean rejected
  ) {
    if (!published || !rejected) {
      throw new IllegalArgumentException(
        String.format(
          "Currently only both published and rejected are supported as return"
        )
      );
    }
    return query(
      String.format(
        "select t.id, t.content_json " +
          "from tweets_clusters c join tweets_all t " +
          "on c.best_tweet_id = t.id " +
          "where c.is_displayed = 0 " +
          "c.created_timestamp > DATE_SUB(NOW(), INTERVAL %f HOUR) " +
          "union all " +
          "select id, original_json " +
          "from tweets_scheduled t " +
          "where published_timestamp > DATE_SUB(NOW(), INTERVAL %f HOUR)",
        hours, hours
      ),
      set -> {
        List<Tweet> tweets = new ArrayList<>();
        try {
          while (set.next()) {
            Tweet tweet = gson.fromJson(set.getString(2), Tweet.class);
            tweet.setExcluded(true);
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

  public List<Tweet> getTweetsForRun(long runId) {
    return query(
      String.format(
        "select t.id, t.content_json, t.tweet_cleaned, " +
          "t.created_timestamp " +
          "from tweets_all t join clusters_runs c " +
          "on t.id = c.tweet_id " +
          "where c.run_id = %d", runId
      ),
      set -> {
        List<Tweet> tweets = new ArrayList<>();
        try {
          while (set.next()) {
            tweets.add(gson.fromJson(set.getString(2), Tweet.class));
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
