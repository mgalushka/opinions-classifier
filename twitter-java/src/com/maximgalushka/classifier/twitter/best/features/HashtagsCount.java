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
      return 1D;
    }
    if (1 <= feature && feature <= 3) {
      return 0D;
    } else {
      return 3D;
    }
  }

  @Override
  public boolean exclude(Long feature) {
    return feature > 4;
  }
}
