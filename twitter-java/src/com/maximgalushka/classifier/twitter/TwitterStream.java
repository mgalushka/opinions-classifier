package com.maximgalushka.classifier.twitter;

import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.classify.Classification;
import com.maximgalushka.classifier.twitter.classify.ClassifyClient;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.twitter.hbc.core.Client;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @since 8/11/2014.
 */
public class TwitterStream {

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

        Gson gson = new Gson();

        final PrintWriter pw = new PrintWriter(args[0]);
        TwitterClient client = new TwitterClient();

        BlockingQueue<String> q = new ArrayBlockingQueue<String>(100);
        final Client hosebirdClient = client.stream(q);
        hosebirdClient.connect();

        ClassifyClient cc = new ClassifyClient();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown");
                pw.close();
                hosebirdClient.stop();
            }
        });
        while (true) {
            try {
                String json = q.take();
                Tweet tweet = gson.fromJson(json, Tweet.class);
                String tweetSingleLine = tweet.getText().trim().replaceAll("\r?\n", " ");
                pw.println(tweetSingleLine);
                if (!NO_CLASSIFICATION) {
                    Classification classification = cc.classify(tweet.getText());
                    System.out.printf("%s -> %s\n", classification, tweetSingleLine);
                } else {
                    System.out.printf("%s\n", tweetSingleLine);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pw.flush();
        }
    }
}
