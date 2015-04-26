package com.maximgalushka.classifier.twitter.best.features;

import com.maximgalushka.classifier.twitter.best.Feature;
import com.maximgalushka.classifier.twitter.model.Tweet;

import java.util.regex.Pattern;

/**
 *
 */
public class CapsLockWordsPercentage implements Feature<Double, Tweet> {

  private static final Pattern CAPSLOCK_WORD_PATTERN = Pattern.compile("[^a-z]+");

  @Override
  public Double extract(Tweet tweet) {
    String text = tweet.getText();
    String[] tokens = text.split("\\s+");
    int size = tokens.length;
    int capslock = 0;
    for (String token : tokens) {
      if (CAPSLOCK_WORD_PATTERN.matcher(token).matches()) {
        capslock++;
      }
    }
    return ((double) capslock) / size;
  }

  @Override
  public double metric(Double feature) {
    return feature;
  }

  @Override
  public boolean exclude(Double feature) {
    // TODO: this is guess - half of all words, make configurable
    // ideally - we need to perform a test to determine optimal value for such thresholds
    return feature >= 0.5D;
  }
}
