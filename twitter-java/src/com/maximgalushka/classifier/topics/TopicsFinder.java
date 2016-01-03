package com.maximgalushka.classifier.topics;

import com.maximgalushka.classifier.twitter.model.Tweet;

import java.util.List;

/**
 * Finds best topics for list of input tweets.
 * We will use Latend Dirichlet Allocation algorithm (Mahout implementation)
 *
 * @author Maxim Galushka
 */
public interface TopicsFinder {

  List<String> findTopics(List<Tweet> documents, int number) throws Exception;
}
