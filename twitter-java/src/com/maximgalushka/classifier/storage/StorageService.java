package com.maximgalushka.classifier.storage;

import com.maximgalushka.classifier.storage.memcached.MemcachedService;
import com.maximgalushka.classifier.storage.mysql.MysqlService;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.clusters.TweetsCluster;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.carrot2.core.Cluster;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Storage service which is based on 2 main services:
 * memcached and mysql.
 * <p>
 * memcached is used to store in real0time in-memory
 * mysql is used as a backup to keep everything between restarts.
 * <p>
 * Now storage service is working in sync way with memcached and async with
 * mysql
 *
 * @since 9/16/2014.
 */
@SuppressWarnings("UnusedDeclaration")
@Resource(name = "storage")
public class StorageService {

  public static final Logger log = Logger.getLogger(StorageService.class);

  private static MemcachedService memcached;
  private static MysqlService mysql;

  private static final StorageService service = new StorageService();
  private static final ExecutorService executor = Executors
    .newFixedThreadPool(3);

  private StorageService() {
  }

  public static void setMemcached(MemcachedService memcached) {
    StorageService.memcached = memcached;
  }

  public static void setMysql(MysqlService mysql) {
    StorageService.mysql = mysql;
  }

  public static StorageService getService() {
    return service;
  }

  public List<TweetsCluster> findClusters(long delta) {
    checkConsistency();
    // from memory by default
    List<TweetsCluster> results = memcached.mergeFromTimestamp(delta);
    if (results.isEmpty()) {
      log.warn(
        String.format(
          "Memcached is empty - loading clusters from database for [%d]",
          delta
        )
      );
      int MAX_RETRIES = 50;
      HashMap<Long, Clusters> fromdb = null;
      int retry = 1;
      while ((fromdb == null || fromdb.isEmpty()) && retry++ <= MAX_RETRIES) {
        long increased = retry * delta;
        log.debug(
          String.format(
            "Trying to reload from mysql for latest [%d] millis",
            increased
          )
        );
        fromdb = mysql.loadClusters(increased);
      }
      if (fromdb == null || fromdb.isEmpty()) {
        log.error(
          String.format(
            "Cannot find anything in mysql for latest [%d] millis." +
              "Just loading top 20 clusters from database.",
            MAX_RETRIES * delta
          )
        );
        fromdb = mysql.loadLastClusters(20L);
      }
      for (long timestamp : fromdb.keySet()) {
        Clusters cls = fromdb.get(timestamp);
        try {
          memcached.saveClustersGroup(timestamp, cls);
        } catch (Exception exception) {
          log.error(
            String.format(
              "Cannot save clusters group for: [%d]: %s",
              timestamp,
              exception
            )
          );
        }
      }
      // try once more after refresh from DB
      return memcached.mergeFromTimestamp(delta);
    }
    return results;
  }

  public void saveNewClustersGroup(final Clusters group) {
    checkConsistency();
    long timestamp = 0L;
    try {
      // save in memcached in sync way
      timestamp = memcached.createNewClustersGroup(group);
    } catch (Exception e) {
      log.error(String.format("Cannot save new clusters group: %s", e));
    }

    // save in db async way
    final long finalTimestamp = timestamp;
    executor.submit(
      () -> {
        if (finalTimestamp != 0L) {
          mysql.saveNewClustersGroup(finalTimestamp, group);
        } else {
          log.error(
            String.format(
              "Error happened during saving in group memcached, timestamp " +
                "== 0. Skipping mysql save."
            )
          );
        }
      }
    );
  }

  public List<Tweet> getLatestTweets(long hours) {
    return mysql.getLatestTweets(hours);
  }

  public void saveTweetsBatch(Collection<Tweet> tweets) {
    mysql.saveTweetsBatch(tweets);
  }

  public void saveTweetsCleanedBatch(Collection<Tweet> tweets) {
    mysql.saveTweetsCleanedBatch(tweets);
  }

  public Long getMaxRunId() throws Exception {
    return mysql.getMaxRunId();
  }

  public Long createNewCluster(Cluster cluster, long maxRunId)
  throws Exception {
    return mysql.createNewCluster(cluster, maxRunId);
  }

  public void saveTweetsClustersBatch(
    Cluster cluster,
    long nextRunId,
    List<Tweet> tweetsInCluster
  ) {
    try {
      long clusterId = createNewCluster(cluster, nextRunId);
      mysql.saveTweetsClustersBatch(clusterId, tweetsInCluster);
    } catch (Exception e) {
      log.error("", e);
      e.printStackTrace();
    }
  }

  private void checkConsistency() {
    if (memcached == null) {
      log.error(
        "Memcached instance is null. Check spring configuration!"
      );
    }
    if (mysql == null) {
      log.error(
        "Mysql instance is null. Check spring configuration!"
      );
    }
  }

  public List<Tweet> getTweetsForRun(long runId) {
    return mysql.getTweetsForRun(runId);
  }
}
