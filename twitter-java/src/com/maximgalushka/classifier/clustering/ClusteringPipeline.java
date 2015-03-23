package com.maximgalushka.classifier.clustering;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.cleanup.CleanPipeline;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.*;

import java.util.*;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public class ClusteringPipeline {

  public static final Logger log = Logger.getLogger(ClusteringPipeline.class);

  private StorageService storage;
  private Controller controller;
  private CleanPipeline cleanPipeline;

  public ClusteringPipeline() {
  }

  public StorageService getStorage() {
    return storage;
  }

  public void setStorage(StorageService storage) {
    this.storage = storage;
  }

  public Controller getController() {
    return controller;
  }

  public void setController(Controller controller) {
    this.controller = controller;
  }

  public CleanPipeline getCleanPipeline() {
    return cleanPipeline;
  }

  public void setCleanPipeline(CleanPipeline cleanPipeline) {
    this.cleanPipeline = cleanPipeline;
  }

  private static final int LATEST_HOURS = 24;

  /**
   * Business method:
   * retrieved tweets for latest 24 hours and apply
   * clustering algorithm to them, then store clusters back to
   * database.
   */
  public void clusterFromStorage() {
    log.info(
      String.format(
        "Starting tweets classifier for latest %d hours",
        LATEST_HOURS
      )
    );
    List<Tweet> latestHoursTweets = storage.getLatestTweets(LATEST_HOURS);
    if (latestHoursTweets.isEmpty()) {
      log.error(
        String.format(
          "Could not find any clusters for latest %d hours. Check if twitter " +
            "stream is running.",
          LATEST_HOURS
        )
      );
      return;
    } else {
      log.info(
        String.format(
          "Found [%d] clusters in database, starting clustering.",
          latestHoursTweets.size()
        )
      );
    }

    cleanPipeline.batchClean(latestHoursTweets);
    storage.saveTweetsCleanedBatch(latestHoursTweets);
    List<Document> docs = readTweetsToDocs(latestHoursTweets);

    // helper map to extract any required tween metadata
    Map<String, Tweet> tweetsIndex = readTweetsToMap(latestHoursTweets);

    // Perform clustering by topic using the Lingo algorithm.
    final ProcessingResult byTopicClusters = controller.process(
      docs,
      null,
      LingoClusteringAlgorithm.class
    );
    final List<Cluster> clustersByTopic = byTopicClusters.getClusters();
    log.debug(
      String.format(
        "As result of clustering latest %d hours tweets, got: [%d] clusters",
        LATEST_HOURS,
        clustersByTopic.size()
      )
    );
    try {
      // next run id is just incremented max run id
      long nextRunId = storage.getMaxRunId() + 1;
      log.debug(
        String.format(
          "Next run ID: [%d]",
          nextRunId
        )
      );
      for (Cluster cluster : clustersByTopic) {
        final List<Tweet> tweetsInCluster = new ArrayList<>();
        for (Document d : cluster.getDocuments()) {
          Tweet t = tweetsIndex.get(d.getStringId());
          tweetsInCluster.add(t);
        }
        log.debug(
          String.format(
            "Storing cluster in database: [%s]",
            cluster.getLabel()
          )
        );
        // creates new cluster in database and associates all tweets with it
        storage.saveTweetsClustersBatch(cluster, nextRunId, tweetsInCluster);
      }
    } catch (Exception e) {
      log.error("", e);
      e.printStackTrace();
    }
  }

  /**
   * Reads tweets to document list ready for classification.<br/>
   * Filters out any duplicate tweets (with dame tweet id).
   */
  private List<Document> readTweetsToDocs(List<Tweet> tweets) {
    List<Document> docs = new ArrayList<>(tweets.size());
    Set<String> set = new HashSet<>(2 * docs.size());
    for (Tweet t : tweets) {
      String id = Long.toString(t.getId());
      if (!set.contains(id)) {
        docs.add(
          new Document(
            null,
            t.getText(),
            null,
            LanguageCode.ENGLISH,
            id
          )
        );
        set.add(id);
      } else {
        log.debug(String.format("Skip duplicate document: [%s]", id));
      }
    }
    return docs;
  }

  private Map<String, Tweet> readTweetsToMap(List<Tweet> tweets) {
    Map<String, Tweet> map = new HashMap<>(2 * tweets.size());
    for (Tweet t : tweets) {
      map.put(Long.toString(t.getId()), t);
    }
    return map;
  }
}
