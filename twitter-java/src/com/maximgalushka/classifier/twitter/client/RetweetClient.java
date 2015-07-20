package com.maximgalushka.classifier.twitter.client;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.account.TwitterAccount;
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
    if (args.length < 2) {
      log.error("Cannot run with empty tweet id to re-tweet.");
      return;
    }

    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );
    TwitterStandardClient client = (TwitterStandardClient) ac.getBean(
      "twitter-stream-client"
    );
    StorageService storage = (StorageService) ac.getBean("storage");

    long tweetId = Long.parseLong(args[0]);
    long accountId = Long.parseLong(args[1]);
    log.debug(
      String.format(
        "Re-tweeting [%d] from account [%d]",
        tweetId,
        accountId
      )
    );

    TwitterAccount account = storage.getAccountById(accountId);
    client.retweet(account, tweetId);
    log.debug("Re-tweeted successfully");
    System.exit(0);
  }
}
