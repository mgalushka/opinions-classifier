package com.maximgalushka.classifier.twitter.stream;

import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.TwitterClient;
import com.maximgalushka.classifier.twitter.classify.carrot.ClusteringTweetsList;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.twitter.hbc.core.Client;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.maximgalushka.classifier.twitter.classify.Tools.cleanFromStart;
import static com.maximgalushka.classifier.twitter.classify.Tools.slice;

/**
 * @since 8/11/2014.
 */
public class TwitterStreamProcessor implements Runnable {
    public static final Logger log = Logger.getLogger(TwitterStreamProcessor.class);

    private Clusters model;
    private LocalSettings settings = LocalSettings.settings();

    public TwitterStreamProcessor(Clusters model) {
        this.model = model;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        boolean useProxy = Boolean.parseBoolean(settings.value(LocalSettings.USE_PROXY));
        if (useProxy) {
            System.setProperty("http.proxyHost", "localhost");
            System.setProperty("http.proxyPort", "4545");
        }

        ClusteringTweetsList clustering = ClusteringTweetsList.getAlgorithm();
        Gson gson = new Gson();
        TwitterClient client = new TwitterClient();

        BlockingQueue<String> q = new ArrayBlockingQueue<String>(100);
        final Client hosebirdClient = client.stream(q);
        hosebirdClient.connect();

        int BATCH_SIZE = 1000;

        // TODO: experiment to find better ratio
        int STEP = BATCH_SIZE / 20;

        ArrayDeque<Tweet> batch = new ArrayDeque<Tweet>();
        while (true) {
            try {
                String json = q.take();
                Tweet tweet = gson.fromJson(json, Tweet.class);
                try {
                    if (batch.size() < (BATCH_SIZE + STEP) && (batch.size() % STEP) == 0) {
                        log.debug("Pre-batch cluster estimation.");
                        clustering.classify(slice(batch, batch.size() - STEP, batch.size()), model);
                    }
                    // we need to collect full batch of elements and then classify the whole batch
                    if (batch.size() == (BATCH_SIZE + STEP)) {
                        log.debug("Clean model. We start full scale clustering.");
                        model.cleanClusters();

                        log.debug("Start batch processing");
                        clustering.classify(slice(batch, BATCH_SIZE), model);

                        // remove first STEP elements from start
                        cleanFromStart(batch, STEP);
                    } else {
                        log.debug(tweet);
                        batch.addLast(tweet);
                    }
                } catch (IOException e) {
                    log.error(e);
                }
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }
}
