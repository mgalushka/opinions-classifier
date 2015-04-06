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
}
