package com.maximgalushka.classifier.twitter.best.features;

import com.maximgalushka.classifier.twitter.best.TextCounterFeature;

/**
 *
 */
public class WordCount implements TextCounterFeature {

  @Override
  public Long extract(String text) {
    return (long) text.split("\\s+").length;
  }

  @Override
  public double metric(Long feature) {
    return 10D / feature;
  }
}
