package com.maximgalushka.classifier.clustering;

import com.maximgalushka.classifier.storage.StorageService;
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

  /**
   * Business method:
   * retrieved tweets for latest 24 hours and apply
   * clustering algorithm to them, then store clusters back to
   * database.
   */
  public void clusterFromStorage() {
    log.info("Starting tweets classifier for latest 24 hours");
    List<Tweet> latest24hours = storage.getLatestTweets(24);
    if (latest24hours.isEmpty()) {
      log.error(
        "Could not find any clusters for latest 24 hours. Check if twitter " +
          "stream is running."
      );
      return;
    } else {
      log.info(
        String.format(
          "Found [%d] clusters in database, starting clustering.",
          latest24hours.size()
        )
      );
    }
    List<Tweet> documents = new ArrayList<>(latest24hours.size());
    List<Document> docs = readTweetsToDocs(documents);

    // helper map to extract any required tween metadata
    Map<String, Tweet> tweetsIndex = readTweetsToMap(documents);

    // Perform clustering by topic using the Lingo algorithm.
    final ProcessingResult byTopicClusters = controller.process(
      docs,
      null,
      LingoClusteringAlgorithm.class
    );
    final List<Cluster> clustersByTopic = byTopicClusters.getClusters();
    try {
      // next run id is just incremented max run id
      long nextRunId = storage.getMaxRunId() + 1;
      for (Cluster cluster : clustersByTopic) {
        final List<Tweet> tweetsInCluster = new ArrayList<>();
        for (Document d : cluster.getDocuments()) {
          Tweet t = tweetsIndex.get(d.getStringId());
          tweetsInCluster.add(t);
        }
        // creates new cluster in database and associates all tweets with it
        storage.saveTweetsClustersBatch(cluster, nextRunId, tweetsInCluster);
      }
    } catch (Exception e) {
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
