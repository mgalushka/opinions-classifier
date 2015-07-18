package com.maximgalushka.classifier.clustering;

import com.maximgalushka.classifier.clustering.client.ClassifierClient;
import com.maximgalushka.classifier.clustering.graphs.DepthFirstSearch;
import com.maximgalushka.classifier.clustering.graphs.Graph;
import com.maximgalushka.classifier.clustering.lsh.simhash.SimhashDuplicates;
import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.account.TwitterAccount;
import com.maximgalushka.classifier.twitter.best.ClusterRepresentativeFinder;
import com.maximgalushka.classifier.twitter.best.FeaturesExtractorPipeline;
import com.maximgalushka.classifier.twitter.cleanup.BlacklistProcessor;
import com.maximgalushka.classifier.twitter.cleanup.CleanPipeline;
import com.maximgalushka.classifier.twitter.client.TwitterStandardClient;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.*;

import java.util.*;

/**
 * Performs logic for full end-to-end clustering process:
 * 1. Retrieves potential tweets from db and clean-up according to
 * black-listed rules.
 * 2. Do cleanup according to features (if thresholds are violated)
 * 3. Cleans tweet texts to remove training links from text, "RT" from start
 * etc...
 * 4. Saves everything in database and performs clustering
 * 5. Chooses best representative from each cluster
 * 6. Retrieves from db all tweets previously published or rejected for
 * latest X days
 * 7. De-duplicates with simhash against already published or rejected tweets
 * to avoid multiple
 * duplicates to be displayed to user.
 * 8. Saves everything back to db.
 * <p>
 * This process is run periodically and stores each subsequent run under
 * incremented run id.
 *
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public class ClusteringPipeline {

  public static final Logger log = Logger.getLogger(ClusteringPipeline.class);

  private StorageService storage;
  private Controller controller;
  private CleanPipeline cleanPipeline;
  private ClusterRepresentativeFinder representativeFinder;
  private TwitterStandardClient twitterClient;
  private BlacklistProcessor blacklistProcessor;
  private FeaturesExtractorPipeline featuresExtractor;

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

  public FeaturesExtractorPipeline getFeaturesExtractor() {
    return featuresExtractor;
  }

  public void setFeaturesExtractor(
    FeaturesExtractorPipeline
      featuresExtractor
  ) {
    this.featuresExtractor = featuresExtractor;
  }

  private static final double LATEST_HOURS = 0.5D;

  public void clusterAllAccounts() {
    List<TwitterAccount> accounts = storage.getActiveAccounts();
    for (TwitterAccount account : accounts) {
      clusterFromStorage(account);
    }
  }

  /**
   * Business method:
   * retrieved tweets for latest X hours and apply
   * clustering algorithm to them, then store clusters back to
   * database.
   */
  public void clusterFromStorage(TwitterAccount account) {
    log.info(
      String.format(
        "Starting tweets classifier for account [%d] for latest %f hours",
        account.getId(),
        LATEST_HOURS
      )
    );
    List<Tweet> latestHoursTweets = storage.getLatestTweets(
      account.getId(),
      LATEST_HOURS
    );
    if (latestHoursTweets.isEmpty()) {
      log.error(
        String.format(
          "Could not find any clusters for account %d for latest %f hours. " +
            "Check if twitter stream is running.",
          account.getId(),
          LATEST_HOURS
        )
      );
      return;
    } else {
      log.info(
        String.format(
          "Found [%d] clusters in database for latest %f hours for account " +
            "%d," +
            "starting clustering.",
          latestHoursTweets.size(),
          LATEST_HOURS,
          account.getId()
        )
      );
    }

    List<Tweet> cleaned4Clustering =
      blacklistProcessor.clean(account, latestHoursTweets);

    log.info(
      String.format(
        "Black-listed tweets removed: [%d], total for clustering: [%d]",
        latestHoursTweets.size() - cleaned4Clustering.size(),
        cleaned4Clustering.size()
      )
    );

    // extract features and clean tweets before clustering based on features
    // TODO: in current design we are extracting features twice:
    // 1 - pre-cleaning
    // 2 - choosing best representative tweet from cluster.
    // this is not an issue if number of clusters is small as we anyway are
    // doing everything in
    // memory
    featuresExtractor.processBatch(latestHoursTweets);

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
      long nextRunId = storage.getMaxRunId(account.getId()) + 1;
      log.debug(
        String.format(
          "Next run ID for account [%d]: [%d]",
          account.getId(),
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
          account.getId(),
          cluster,
          nextRunId,
          tweetsInCluster
        );
        //countId.put(tweetsInCluster.size(), clusterId);

        // NOTE: this method internally exclude tweets (mark them as excluded
        // - and this will be store in database after this call!)
        // based on same metrics
        // NOT: this is not clear from code that this will be done
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
      }

      // retrieve current clusters for run
      // get used tweets
      // re-cluster
      // go BFS and mark de-duplicated clusters as needed.
      List<Tweet> current = storage.getBestTweetsForRun(
        account.getId(),
        nextRunId
      );
      // TODO: make configurable
      final List<Tweet> used = storage.getLatestUsedTweets(
        account.getId(),
        7 * 24D,
        true,
        true
      );
      final int usedSize = used.size();

      used.addAll(current);

      // TODO: make distance parameter configurable
      SimhashDuplicates duplicates = new SimhashDuplicates(10);
      Graph graph = duplicates.buildTweetsGraph(used);
      final List<Tweet> update = new ArrayList<>();

      // shared DFS object
      final DepthFirstSearch dfs = new DepthFirstSearch(
        graph,
        vertexId -> {
          // if already excluded - don't add to excluded list
          if (vertexId < usedSize) {
            return;
          }
          Tweet tweet = used.get(vertexId);
          tweet.setExcluded(true);
          // TODO: specify exactly
          tweet.setExcludedReason(
            String.format(
              "[Already published or rejected]%s",
              tweet.getExcludedReason()
            )
          );
          update.add(tweet);
        }
      );

      for (int index = 0; index < usedSize; index++) {
        // if there are self-duplicates - DFS will prevent 2nd time
        // traversing same
        // connected component. So this is optimized to be linear on the
        // number of
        // vertices in the graph.
        dfs.dfs(index);
      }

      // TODO: idea on how to speed up checking if new best representatives are
      // TODO same tweets that were published previously:
      // let first N tweets are existing tweets (published already or rejected
      // already)
      // build a graph and after it go through connected components starting
      // from
      // 1..N for each already twitted/rejected tweet.
      // and mark all tweets in this CC as duplicates with corresponding reason
      // to spidify the process - if CC was marked - don't repeat it
      // if there are self-duplicates across 1..N by themselves.
      // 1..N - to choose for latest 30 days to avoid huge number of messages.

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

      // Now we are calling classifier web service for each best tweet to get
      // label (pos/neg) and in future - probability score that this tweet is
      // good for publishing
      List<Tweet> best = storage.getBestTweetsForRun(
        account.getId(),
        nextRunId
      );
      ClassifierClient clClient = new ClassifierClient();
      for (Tweet tweet : best) {
        String label = clClient.getLabel(tweet.getText());
        if (label != null) {
          storage.saveTweetLabel(tweet, label);
        } else {
          log.warn(
            String.format(
              "Labelling failed and returned null for tweet: [%s]",
              tweet
            )
          );
        }
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
