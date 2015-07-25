package com.maximgalushka.classifier.twitter.best;

/**
 * Feature interface.
 * All features used to extract best tweet in cluster should extend it.
 * <p>
 * Feature#exclude method says if the whole tweet should be excluded completely
 * based on feature value and used for filtering bad quality clusters.
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
   * @return true if based on current metric tweet should be excluded from
   * displaying or banned
   */
  boolean exclude(T feature);
}
