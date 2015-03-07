package com.maximgalushka.classifier.clustering;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.classify.carrot.ClusteringTweetsListAlgorithm;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Maxim Galushka
 */
public class ClusteringPipeline {

  public static final Logger log = Logger.getLogger(ClusteringPipeline.class);

  private StorageService storage;
  private ClusteringTweetsListAlgorithm clustering;
  private Controller controller;

  public ClusteringPipeline() {
  }

  public StorageService getStorage() {
    return storage;
  }

  public void setStorage(StorageService storage) {
    this.storage = storage;
  }

  public ClusteringTweetsListAlgorithm getClustering() {
    return clustering;
  }

  public void setClustering(ClusteringTweetsListAlgorithm clustering) {
    this.clustering = clustering;
  }

  public Controller getController() {
    return controller;
  }

  public void setController(Controller controller) {
    this.controller = controller;
  }

  /**
   * Business method:
   *  retrieved tweets for latest 24 hours and apply
   *  clustering algorithm to them, then store clusters back to
   *  database.
   */
  public void clusterFromStorage() {
    List<Tweet> latest24hours = storage.getLatestTweets(24);
    if (latest24hours.isEmpty()) {
      log.error(
        "Could not find any clusters for latest 24 hours. Check if twitter " +
          "stream is running."
      );
      return;
    }
    List<Tweet> documents = new ArrayList<>(latest24hours.size());
    List<Document> docs = readTweetsToDocs(documents);

    // helper map to extract any required tween metadata
    //Map<String, Tweet> tweetsIndex = readTweetsToMap(batch);

    // Perform clustering by topic using the Lingo algorithm.
    final ProcessingResult byTopicClusters = controller.process(
      docs,
      null,
      LingoClusteringAlgorithm.class
    );
    final List<Cluster> clustersByTopic = byTopicClusters.getClusters();

    // TODO
    storage.saveTweetsClustersBatch(null);
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
}
