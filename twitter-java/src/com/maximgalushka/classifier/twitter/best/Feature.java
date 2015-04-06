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
}
