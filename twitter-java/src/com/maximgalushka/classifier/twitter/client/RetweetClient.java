package com.maximgalushka.classifier.twitter.client;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import twitter4j.TwitterException;

import java.io.IOException;

/**
 * @author Maxim Galushka
 */
public class RetweetClient {

  public static final Logger log = Logger.getLogger(RetweetClient.class);

  public static void main(String[] args) throws IOException, TwitterException {
    if (args.length == 0) {
      log.error(String.format("Cannot run with empty tweet id to re-tweet."));
      return;
    }

    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );
    TwitterStandardClient client = (TwitterStandardClient) ac.getBean(
      "twitter-stream-client"
    );

    long tweetId = Long.parseLong(args[0]);
    log.debug(
      String.format(
        "Re-tweeting [%d] from account",
        tweetId
      )
    );
    client.retweet(tweetId);
    log.debug("Re-tweeted successfully");
    System.exit(0);
  }
}
