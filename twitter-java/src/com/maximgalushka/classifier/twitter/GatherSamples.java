package com.maximgalushka.classifier.twitter;

import com.maximgalushka.classifier.twitter.client.TwitterStandardClient;
import com.maximgalushka.classifier.twitter.model.Statuses;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

/**
 * @author Maxim Galushka
 */
public class GatherSamples {

  public static final Logger log = Logger.getLogger(GatherSamples.class);

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length == 0) {
      return;
    }

    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );
    TwitterStandardClient client = (TwitterStandardClient) ac.getBean("twitter-client");

    PrintWriter pw = new PrintWriter(args[0]);
    String token = client.oauth();

    Statuses statuses = client.search(token, "Ukraine", 0);
    List<Tweet> tweets = statuses.getTweets();

    HashSet<String> texts = new HashSet<>();
    for (Tweet t : tweets) {
      boolean to_add = true;
      String text = t.getText()
                     .replaceAll("\\s+", " ")
                     .replaceAll("\\.+", ".")
                     .trim();
      if (t.isRetweeted()) {
        to_add = false;
      }
      if (texts.contains(text)) {
        log.trace(String.format("Duplicate: [%s]\n", text));
        to_add = false;
      }
      texts.add(text);
      if (to_add) {
        pw.println(
          String.format(
            "'%s, %s'",
            text,
            t.getAuthor().getScreenName()
          )
        );
        log.debug(String.format("%s", t));
      }
    }
    pw.flush();
    pw.close();
  }


}
