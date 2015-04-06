package com.maximgalushka.classifier.twitter.best.features;

import com.maximgalushka.classifier.twitter.best.PatternCounterFeature;

import java.util.regex.Pattern;

/**
 *
 */
public class HashtagsCount extends PatternCounterFeature {

  private static final Pattern HASHTAG_PATTERN = Pattern.compile("#\\S+");

  @Override
  protected Pattern getPattern() {
    return HASHTAG_PATTERN;
  }

  @Override
  public double metric(Long feature) {
    if (feature == 0) {
      return 0D;
    }
    if (feature <= 2) {
      return 2D - feature;
    } else {
      return 2D;
    }
  }
}
