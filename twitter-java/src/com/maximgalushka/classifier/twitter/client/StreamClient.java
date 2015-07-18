package com.maximgalushka.classifier.twitter.client;

import com.maximgalushka.classifier.twitter.account.TwitterAccount;
import com.maximgalushka.classifier.twitter.model.Tweet;
import java.util.concurrent.BlockingQueue;

/**
 * @author Maxim Galushka
 */
public interface StreamClient {

  /**
   * Streams messages from twitter to output queue
   *
   * @param account twitter account
   * @param term   search term to stream for
   * @param output output blocking queue
   */
  void stream(TwitterAccount account, String term, BlockingQueue<Tweet> output);
}
