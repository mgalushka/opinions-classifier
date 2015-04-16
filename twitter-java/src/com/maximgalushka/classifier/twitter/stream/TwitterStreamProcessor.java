package com.maximgalushka.classifier.twitter.stream;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.classify.carrot
  .ClusteringTweetsListAlgorithm;
import com.maximgalushka.classifier.twitter.client.StreamClient;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;

import java.util.ArrayDeque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @since 8/11/2014.
 */
public class TwitterStreamProcessor implements Runnable {
  public static final Logger log = Logger.getLogger(
    TwitterStreamProcessor
      .class
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

    BlockingQueue<Tweet> q = new ArrayBlockingQueue<>(1000);
    // twitter steam client is infinitely sends messages to this queue
    // in separate thread and we will read from it and process
    streamClient.stream(
      settings.value(
        LocalSettings.TWITTER_KEYWORDS
      ),
      q
    );

    int BATCH_SIZE = 20; // much slow for ML domain

    // TODO: experiment to find better ratio
    //int STEP = BATCH_SIZE / 20;

    ArrayDeque<Tweet> batch = new ArrayDeque<>();
    long messageCount = 0;
    while (!this.stopping) {
      try {
        Tweet tweet = q.take();
        log.trace(tweet);
        if (batch.size() >= BATCH_SIZE) {
          storage.saveTweetsBatch(batch);
          batch.clear();
        } else {
          batch.add(tweet);
        }
        // TODO: final step - we turn off old way of processing tweets.
        /*
        try {
          if (batch.size() < (BATCH_SIZE + STEP) &&
            (batch.size() % STEP) == 0) {
            // saving current batch to database
            log.debug("Saving current tweets batch to database");
            storage.saveTweetsBatch(batch);

            log.debug("Pre-batch cluster estimation.");
            clustering.classify(
              slice(batch, batch.size() - STEP, batch.size()),
              model
            );
          }
          // we need to collect full batch of elements and then classify the
          // whole batch
          if (batch.size() == (BATCH_SIZE + STEP)) {
            // saving current batch to database
            log.debug("Saving current tweets batch to database");
            storage.saveTweetsBatch(batch);

            log.debug("Clean model. We start full scale clustering.");
            model.cleanClusters();

            log.debug("Start batch processing");
            clustering.classify(slice(batch, BATCH_SIZE), model);

            // remove first STEP elements from start
            cleanFromStart(batch, STEP);
          } else {
            log.trace(String.format("[%d] %s", messageCount++, tweet));
            if(messageCount % 100 == 0){
              log.debug("Receiving twitter messages...");
            }
            batch.addLast(tweet);
          }
        } catch (IOException e) {
          log.error(e);
          e.printStackTrace();
        }
      } catch (InterruptedException e) {
        log.error(e);
        e.printStackTrace();
      }
    }
    */
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
