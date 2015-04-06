package com.maximgalushka.classifier.twitter.best.features;

import com.maximgalushka.classifier.twitter.best.PatternCounterFeature;

import java.util.regex.Pattern;

/**
 *
 */
public class MentionsCount extends PatternCounterFeature{

  private static final Pattern MENTION_PATTERN = Pattern.compile("\\s@\\S+\\s");

  @Override
  protected Pattern getPattern() {
    return MENTION_PATTERN;
  }
}
