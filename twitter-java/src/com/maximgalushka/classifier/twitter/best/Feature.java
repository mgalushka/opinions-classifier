package com.maximgalushka.classifier.twitter.best;

/**
 *
 */
public interface Feature<T, I> {

  /**
   * Extracts feature from tweet text
   *
   * @return some feature
   */
  T extract(I text);

  /**
   * @return metric based on feature
   */
  double metric(T feature);

  /**
   * @return true is based on current metric tweet should be excluded or banned
   */
  boolean exclude(T feature);
}
