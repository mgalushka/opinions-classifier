package com.maximgalushka.classifier.twitter.best.features;

import com.maximgalushka.classifier.twitter.best.Feature;
import com.maximgalushka.classifier.twitter.model.Tweet;

/**
 * @author Maxim Galushka
 */
public class HasMediaFeature implements Feature<Boolean, Tweet> {

  @Override
  public Boolean extract(Tweet tweet) {
    return !(tweet.getEntities() == null ||
      tweet.getEntities().getMedia() == null ||
      tweet.getEntities().getMedia().isEmpty());
  }

  @Override
  public double metric(Boolean feature) {
    return feature ? 0 : 1;
  }

  @Override
  public boolean exclude(Boolean feature) {
    return false;
  }
}
