package com.maximgalushka.classifier.storage.mysql;

import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;

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
            "(id, content_json, created_timestamp)" +
            "values (?, ?, now())"
        )
      ) {
        for(Tweet tweet: tweets) {
          stmt.setLong(1, tweet.getId());
          stmt.setString(2, gson.toJson(tweet));
          stmt.addBatch();
        }
        stmt.executeBatch();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Latest tweets for latest frame in hours
   *
   * @param hours hours to find tweets from
   */
  public List<Tweet> getLatestTweets(long hours) {
    return query(
      String.format(
        "select id, cluster_id, content_json, created_timestamp " +
          "from tweets_all " +
          "where created_timestamp >= DATE_SUB(NOW(), INTERVAL %d HOUR)", hours
      ),
      set -> {
        List<Tweet> tweets = new ArrayList<>();
        try {
          while (set.next()) {
            tweets.add(gson.fromJson(set.getString(3), Tweet.class));
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
    } catch (SQLException e) {
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
