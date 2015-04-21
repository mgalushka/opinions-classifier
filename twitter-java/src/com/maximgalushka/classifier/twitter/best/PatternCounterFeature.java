package com.maximgalushka.classifier.twitter.best;

import com.maximgalushka.classifier.twitter.model.Tweet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public abstract class PatternCounterFeature implements TextCounterFeature {

  @Override
  public Long extract(Tweet tweet) {
    Matcher matcher = getPattern().matcher(tweet.getText());
    long count = 0;
    while (matcher.find()) {
      count++;
    }
    return count;
  }

  protected abstract Pattern getPattern();
}
