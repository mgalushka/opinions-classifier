package com.maximgalushka.classifier.twitter;

import com.maximgalushka.classifier.twitter.model.Tweet;

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
        PrintWriter all = new PrintWriter("D:\\projects\\classifier\\200-all.txt");
        PrintWriter pw = new PrintWriter("D:\\projects\\classifier\\200.txt");
        TwitterClient client = new TwitterClient();
        String token = client.oauth();

        List<Tweet> tweets = client.search(token, "Ukraine");
        System.out.println(tweets);
        all.println(tweets);
        all.flush();
        all.close();

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
