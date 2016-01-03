package com.maximgalushka.classifier.topics;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.account.TwitterAccount;
import com.maximgalushka.classifier.twitter.client.TwitterStandardClient;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Extracts all topics of interest for specific twitter user.
 * Uses his posts.
 *
 * @author Maxim Galushka
 */
public class UserTopicsExtractor {

  private StorageService storage;
  private TopicsFinder finder;
  private TwitterStandardClient twitterClient;

  public UserTopicsExtractor() {
  }

  public void setStorage(StorageService storage) {
    this.storage = storage;
  }

  public void setFinder(TopicsFinder finder) {
    this.finder = finder;
  }

  public void setTwitterClient(TwitterStandardClient twitterClient) {
    this.twitterClient = twitterClient;
  }

  public List<String> findTopicsForUser(TwitterAccount account)
  throws Exception {
    List<Tweet> tweets = twitterClient.getLatestTweets(account);
    return this.finder.findTopics(tweets, 10);
  }

  public static void main(String[] args) throws Exception {
    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );
    UserTopicsExtractor extractor = (UserTopicsExtractor) ac.getBean(
      "topicExtractor"
    );
    try {
      StorageService storage = (StorageService) ac.getBean("storage");
      extractor.findTopicsForUser(storage.getAccountById(1));
    } catch (Exception e){
      e.printStackTrace();
    }
    System.exit(0);
  }
}
