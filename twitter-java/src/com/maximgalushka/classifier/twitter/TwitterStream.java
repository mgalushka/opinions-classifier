package com.maximgalushka.classifier.twitter;

import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.classify.carrot.ClusteringTweetsList;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.twitter.hbc.core.Client;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayDeque;
import java.util.concurrent.*;

import static com.maximgalushka.classifier.twitter.classify.Tools.*;

/**
 * @since 8/11/2014.
 */
public class TwitterStream {

    public static final Logger log = Logger.getLogger(TwitterStream.class);
    private static boolean NO_CLASSIFICATION = false;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length == 0) return;
        if (args.length > 1) {
            String second = args[1];
            if (second != null && !"".equals(second.trim())) {
                if ("--no-classification".equals(second.trim())) {
                    System.out.println("Running stream without classification");
                    NO_CLASSIFICATION = true;
                }
            }
        }

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "4545");

        ClusteringTweetsList clustering = ClusteringTweetsList.getAlgorithm();

        Gson gson = new Gson();

        final PrintWriter pw = new PrintWriter(args[0]);
        TwitterClient client = new TwitterClient();

        BlockingQueue<String> q = new ArrayBlockingQueue<String>(100);
        final Client hosebirdClient = client.stream(q);
        hosebirdClient.connect();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown");
                pw.close();
                hosebirdClient.stop();
            }
        });
        int BATCH_SIZE = 1000;
        // TODO: experiment to find better ratio
        int STEP = BATCH_SIZE / 10;

        ArrayDeque<Tweet> batch = new ArrayDeque<Tweet>();
        while (true) {
            try {
                String json = q.take();
                Tweet tweet = gson.fromJson(json, Tweet.class);
                String tweetSingleLine = tweet.getText().trim().replaceAll("\r?\n", " ");
                if (!NO_CLASSIFICATION) {
                    try {
                        // we need to collect full batch of elements and then classify the whole batch
                        if (batch.size() == (BATCH_SIZE + STEP)) {
                            Clusters clusters = clustering.classify(slice(batch, BATCH_SIZE));
                            log.debug(String.format("%s", clusters));

                            // remove first STEP elements from start
                            cleanFromStart(batch, STEP);
                        } else {
                            batch.addLast(tweet);
                        }
                    } catch (IOException e) {
                        log.error(e);
                    }
                } else {
                    log.debug(String.format("%s", tweetSingleLine));
                }
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }
}
