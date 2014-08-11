package com.maximgalushka.classifier.twitter;

import com.twitter.hbc.core.Client;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @since 8/11/2014.
 */
public class TwitterStream {

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) return;

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "4545");

        PrintWriter pw = new PrintWriter(args[0]);
        TwitterClient client = new TwitterClient();
        String token = client.oauth();

        BlockingQueue<String> q = new ArrayBlockingQueue<String>(100);
        Client hosebirdClient = client.stream(token, q);
        hosebirdClient.connect();

        int c = 10000;
        while (c-- > 0) {
            try {
                String tweet = q.take();
                pw.println(tweet);
                System.out.println(tweet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pw.flush();
        }
        pw.close();
        hosebirdClient.stop();
    }
}
