package com.maximgalushka.classifier.twitter;

import com.maximgalushka.classifier.twitter.model.Tweet;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

/**
 * @author Maxim Galushka
 */
public class GatherSamples {

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) return;

        PrintWriter pw = new PrintWriter(args[0]);
        TwitterClient client = new TwitterClient();
        String token = client.oauth();

        List<Tweet> tweets = client.search(token, "Ukraine");

        HashSet<String> texts = new HashSet<String>();
        for (Tweet t : tweets) {
            boolean to_add = true;
            String text = t.getText().replaceAll("\\s+", " ").replaceAll("\\.+", ".").trim();
            if (!t.isRetweeted()) to_add = false;
            if (texts.contains(text)) {
                System.out.printf("Duplicate: [%s]\n", text);
                to_add = false;
            }
            texts.add(text);
            if (to_add) {
                pw.println(String.format("'%s, %s'", text, t.getAuthor().getScreenName()));
            }
        }
        pw.flush();
        pw.close();
    }


}
