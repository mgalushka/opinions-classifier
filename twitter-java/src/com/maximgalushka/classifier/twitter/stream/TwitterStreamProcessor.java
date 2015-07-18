package com.maximgalushka.classifier.twitter.stream;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.account.TwitterAccount;
import com.maximgalushka.classifier.twitter.classify.carrot
  .ClusteringTweetsListAlgorithm;
import com.maximgalushka.classifier.twitter.client.StreamClient;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @since 8/11/2014.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class TwitterStreamProcessor implements Runnable {
  public static final Logger log = Logger.getLogger(
    TwitterStreamProcessor.class
  );

  private Clusters model;
  private StreamClient streamClient;
  private ClusteringTweetsListAlgorithm clustering;
  private LocalSettings settings;
  private StorageService storage;

  private volatile boolean stopping = false;

  public TwitterStreamProcessor() {
  }

  public void setClusters(Clusters model) {
    this.model = model;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setSettings(LocalSettings settings) {
    this.settings = settings;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setClustering(ClusteringTweetsListAlgorithm clustering) {
    this.clustering = clustering;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setStreamClient(StreamClient streamClient) {
    this.streamClient = streamClient;
  }

  public void sendStopSignal() {
    this.stopping = true;
  }

  public void setModel(Clusters model) {
    this.model = model;
  }

  public StorageService getStorage() {
    return storage;
  }

  public void setStorage(StorageService storage) {
    this.storage = storage;
  }

  @Override
  @SuppressWarnings("InfiniteLoopStatement")
  public void run() {
    boolean useProxy = Boolean.parseBoolean(
      settings.value(
        LocalSettings.USE_PROXY
      )
    );
    if (useProxy) {
      System.setProperty("http.proxyHost", "localhost");
      System.setProperty("http.proxyPort", "4545");
    }

    List<TwitterAccount> accounts = storage.getActiveAccounts();
    BlockingQueue<Tweet> q = new ArrayBlockingQueue<>(1000);

    log.debug(
      String.format(
        "Extracted %d accounts from DB. ",
        accounts.size()
      )
    );
    for (TwitterAccount account : accounts) {
      log.debug(
        String.format(
          "Starting stream for account [%d]",
          account.getId()
        )
      );
      // twitter steam client is infinitely sends messages to this queue
      // in separate thread and we will read from it and process
      streamClient.stream(
        account,
        settings.value(
          LocalSettings.TWITTER_KEYWORDS
        ),
        q
      );
    }

    int BATCH_SIZE = Math.min(100, 20 * accounts.size());

    ArrayDeque<Tweet> batch = new ArrayDeque<>();
    long messageCount = 0;
    while (!this.stopping) {
      try {
        Tweet tweet = q.take();
        log.debug(tweet);
        if (batch.size() >= BATCH_SIZE) {
          storage.saveTweetsBatch(batch);
          batch.clear();
        } else {
          batch.add(tweet);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
