package com.maximgalushka.classifier.twitter.best;

/**
 *
 */
public interface Feature<T, I> {

  /**
   * Extracts feature from tweet text
   *
   * @param text
   * @return some feature
   */
  T extract(I text);
}
