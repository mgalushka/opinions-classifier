package com.maximgalushka.classifier.twitter;

import com.maximgalushka.classifier.twitter.model.Tweet;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author Maxim Galushka
 */
public class GatherSamples {

    public List<String> samples(String query) {
        return null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) return;

        PrintWriter pw = new PrintWriter(args[0]);
        TwitterClient client = new TwitterClient();
        String token = client.oauth();

        List<Tweet> tweets = client.search(token, "Ukraine");

        for (Tweet t : tweets) {
            if (!t.isRetweeted()) {
                String text = t.getText().replaceAll("\\s+", " ");
                pw.println(String.format("'%s, %s'", text, t.getAuthor().getScreenName()));
            }
        }
        pw.flush();
        pw.close();
    }



}
