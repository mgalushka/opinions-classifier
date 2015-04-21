package com.maximgalushka.classifier.twitter.best.features;

import com.maximgalushka.classifier.twitter.best.TextCounterFeature;
import com.maximgalushka.classifier.twitter.model.Tweet;

/**
 *
 */
public class WordCount implements TextCounterFeature {

  @Override
  public Long extract(Tweet tweet) {
    return (long) tweet.getText().split("\\s+").length;
  }

  @Override
  public double metric(Long feature) {
    return 10D / feature;
  }

  @Override
  public boolean exclude(Long feature) {
    return false;
  }
}
