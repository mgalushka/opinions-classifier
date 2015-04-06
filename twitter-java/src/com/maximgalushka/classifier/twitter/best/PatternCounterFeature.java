package com.maximgalushka.classifier.twitter.best;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public abstract class PatternCounterFeature implements TextCounterFeature {

  @Override
  public Long extract(String text) {
    Matcher matcher = getPattern().matcher(text);
    long count = 0;
    while (matcher.find()) {
      count++;
    }
    return count;
  }

  protected abstract Pattern getPattern();
}
