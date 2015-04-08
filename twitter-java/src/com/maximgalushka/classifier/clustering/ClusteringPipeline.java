package com.maximgalushka.classifier.clustering;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.best.BestClusterFinder;
import com.maximgalushka.classifier.twitter.best.ClusterRepresentativeFinder;
import com.maximgalushka.classifier.twitter.cleanup.CleanPipeline;
import com.maximgalushka.classifier.twitter.client.TwitterStandardClient;
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
  private ClusterRepresentativeFinder representativeFinder;
  private BestClusterFinder clusterFinder;
  private TwitterStandardClient twitterClient;

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

  public ClusterRepresentativeFinder getRepresentativeFinder() {
    return representativeFinder;
  }

  public void setRepresentativeFinder(
    ClusterRepresentativeFinder
      representativeFinder
  ) {
    this.representativeFinder = representativeFinder;
  }

  public BestClusterFinder getClusterFinder() {
    return clusterFinder;
  }

  public void setClusterFinder(BestClusterFinder clusterFinder) {
    this.clusterFinder = clusterFinder;
  }

  public TwitterStandardClient getTwitterClient() {
    return twitterClient;
  }

  public void setTwitterClient(TwitterStandardClient twitterClient) {
    this.twitterClient = twitterClient;
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

    // NOTE: after this step - each tweet's text is cleaned.
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
      TreeMap<Integer, Long> countId = new TreeMap<>();
      Map<Long, Tweet> bestTweetInCluster = new HashMap<>();
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
        long clusterId = storage.saveTweetsClustersBatch(
          cluster,
          nextRunId,
          tweetsInCluster
        );
        countId.put(tweetsInCluster.size(), clusterId);

        ClusterRepresentativeFinder.Pair<Tweet, Map<Tweet, Map<String, Object>>>
          pair = representativeFinder
          .findRepresentativeFeaturesBased(
            tweetsInCluster
          );

        // find best tweet in cluster
        Tweet representative = pair.getA();
        Map<Tweet, Map<String, Object>> features = pair.getB();
        // store all features for all tweets in batch in database
        storage.updateTweetsFeaturesBatch(features);

        storage.saveBestTweetInCluster(
          clusterId,
          representative.getId()
        );
        bestTweetInCluster.put(clusterId, representative);
      }

      Random r = new Random(System.currentTimeMillis());
      boolean retweet = (r.nextInt(10) <= 3); // 30%
      // TODO: this logic should be separated to special handler
      // TODO: which chooses best tweet in cluster and re-tweet or
      // TODO: creates new tweet based o  it.
      int size = countId.size();
      if (size <= 1) {
        log.error(
          String.format(
            "Cannot find best from collection, too low number of elements: [%d]",
            size
          )
        );
        return;
      }
      // TODO: randomize slightly to minimize clashes.
      // TODO: implement algorithm to eliminate posting what was already posted before.
      int choice = r.nextInt(Math.min(size - 1, 3)) + 1;
      Object bestKey = countId.descendingMap().keySet().toArray()[choice];
      long bestCluster = countId.get(bestKey);
      Tweet tweet = bestTweetInCluster.get(bestCluster);
      storage.savePublishedTweet(tweet, retweet);
      if (retweet) {
        log.warn(
          String.format(
            "Re-tweeting [%s]",
            tweet
          )
        );
        twitterClient.retweet(tweet.getId());
      } else {
        log.warn(
          String.format(
            "Straight tweet [%s]",
            tweet
          )
        );
        twitterClient.post(tweet);
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
