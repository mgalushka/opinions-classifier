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

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length == 0) return;

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "4545");

        Gson gson = new Gson();

        PrintWriter pw = new PrintWriter(args[0]);
        TwitterClient client = new TwitterClient();

        BlockingQueue<String> q = new ArrayBlockingQueue<String>(100);
        Client hosebirdClient = client.stream(q);
        hosebirdClient.connect();

        ClassifyClient cc = new ClassifyClient();

        int c = 10000;
        while (c-- > 0) {
            try {
                String json = q.take();
                Tweet tweet = gson.fromJson(json, Tweet.class);
                pw.println(tweet);
                Classification classification = cc.classify(tweet.getText());
                System.out.printf("%s -> %s\n", classification, tweet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pw.flush();
        }
        pw.close();
        hosebirdClient.stop();
    }
}
