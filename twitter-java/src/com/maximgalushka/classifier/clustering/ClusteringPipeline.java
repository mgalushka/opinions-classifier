package com.maximgalushka.classifier.clustering;

import com.maximgalushka.classifier.clustering.graphs.DepthFirstSearch;
import com.maximgalushka.classifier.clustering.graphs.Graph;
import com.maximgalushka.classifier.clustering.lsh.simhash.SimhashDuplicates;
import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.best.BestClusterFinder;
import com.maximgalushka.classifier.twitter.best.ClusterRepresentativeFinder;
import com.maximgalushka.classifier.twitter.cleanup.BlacklistProcessor;
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
  private BlacklistProcessor blacklistProcessor;

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

  public BlacklistProcessor getBlacklistProcessor() {
    return blacklistProcessor;
  }

  public void setBlacklistProcessor(BlacklistProcessor blacklistProcessor) {
    this.blacklistProcessor = blacklistProcessor;
  }

  private static final double LATEST_HOURS = 0.5D;

  /**
   * Business method:
   * retrieved tweets for latest X hours and apply
   * clustering algorithm to them, then store clusters back to
   * database.
   */
  public void clusterFromStorage() {
    log.info(
      String.format(
        "Starting tweets classifier for latest %f hours",
        LATEST_HOURS
      )
    );
    List<Tweet> latestHoursTweets = storage.getLatestTweets(LATEST_HOURS);
    if (latestHoursTweets.isEmpty()) {
      log.error(
        String.format(
          "Could not find any clusters for latest %f hours. Check if twitter " +
            "stream is running.",
          LATEST_HOURS
        )
      );
      return;
    } else {
      log.info(
        String.format(
          "Found [%d] clusters in database for latest %f hours, starting " +
            "clustering.",
          latestHoursTweets.size(),
          LATEST_HOURS
        )
      );
    }

    List<Tweet> cleaned4Clustering =
      blacklistProcessor.clean(latestHoursTweets);

    log.info(
      String.format(
        "Black-listed tweets removed: [%d], total for clustering: [%d]",
        latestHoursTweets.size() - cleaned4Clustering.size(),
        cleaned4Clustering.size()
      )
    );

    // storing processed list as during processing we are
    storage.saveTweetsCleanedBatch(latestHoursTweets);

    // NOTE: after this step - each tweet's text is cleaned.
    cleanPipeline.batchClean(cleaned4Clustering);
    storage.saveTweetsCleanedBatch(cleaned4Clustering);
    List<Document> docs = readTweetsToDocs(cleaned4Clustering);

    // helper map to extract any required tween metadata
    Map<String, Tweet> tweetsIndex = readTweetsToMap(cleaned4Clustering);

    // Perform clustering by topic using the Lingo algorithm.
    final ProcessingResult byTopicClusters = controller.process(
      docs,
      null,
      LingoClusteringAlgorithm.class
    );
    final List<Cluster> clustersByTopic = byTopicClusters.getClusters();
    log.debug(
      String.format(
        "As result of clustering latest %f hours tweets, got: [%d] clusters",
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
      //TreeMap<Integer, Long> countId = new TreeMap<>();
      //Map<Long, Tweet> bestTweetInCluster = new HashMap<>();
      for (Cluster cluster : clustersByTopic) {
        final List<Tweet> tweetsInCluster = new ArrayList<>();
        for (Document d : cluster.getDocuments()) {
          Tweet t = tweetsIndex.get(d.getStringId());
          tweetsInCluster.add(t);
        }
        log.debug(
          String.format(
            "Storing cluster in database: [%s], size = [%d]",
            cluster.getLabel(),
            cluster.getDocuments().size()
          )
        );
        // creates new cluster in database and associates all tweets with it
        long clusterId = storage.saveTweetsClustersBatch(
          cluster,
          nextRunId,
          tweetsInCluster
        );
        //countId.put(tweetsInCluster.size(), clusterId);

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
        //bestTweetInCluster.put(clusterId, representative);
      }

      // retrieve current clusters for run
      // get used tweets
      // re-cluster
      // go BFS and mark de-duplicated clusters as needed.
      List<Tweet> current = storage.getTweetsForRun(nextRunId);
      // TODO: make configurable
      final List<Tweet> used = storage.getLatestUsedTweets(7 * 24D, true, true);
      final int usedSize = used.size();

      used.addAll(current);

      SimhashDuplicates duplicates = new SimhashDuplicates();
      Graph graph = duplicates.buildTweetsGraph(used);
      final List<Tweet> update = new ArrayList<>();

      for (int index = 0; index < usedSize; index++) {
        // TODO: suboptimal - don't process if there are self-duplicates.
        // TODO: screw it for now.
        // TODO: just shit-code it for now.
        DepthFirstSearch dfs = new DepthFirstSearch(
          graph,
          index,
          vertexId -> {
            // if already excluded - don't add to excluded list
            if (vertexId < usedSize) {
              return;
            }
            Tweet tweet = used.get(vertexId);
            tweet.setExcluded(true);
            // TODO: specify exactly
            tweet.setExcludedReason(
              "Already published or rejected"
            );
            update.add(tweet);
          }
        );
      }

      log.debug(
        String.format(
          "Already rejected or published tweets excluded [%d] clusters. " +
            "Saving.",
          update.size()
        )
      );
      log.trace(
        String.format(
          "Excluded tweets: %s",
          update
        )
      );
      storage.saveTweetsCleanedBatch(update);

      // TODO: we have pivoted and don't publish tweets as part of clustering
      // job anymore
      /**
       Random r = new Random(System.currentTimeMillis());
       boolean retweet = (r.nextInt(10) <= 2); // 20%
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
       // TODO: implement algorithm to eliminate posting what was already
       posted before.
       int choice = r.nextInt(Math.min(size - 1, 4)) + 1;
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
       */

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
