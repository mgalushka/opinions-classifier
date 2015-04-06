package com.maximgalushka.classifier.twitter.best;

import java.util.regex.Pattern;

/**
 *
 */
public abstract class PatternCounterFeature implements TextCounterFeature {

  @Override
  public Long extract(String text) {
    return (long) getPattern().matcher(text).groupCount();
  }

  protected abstract Pattern getPattern();
}
