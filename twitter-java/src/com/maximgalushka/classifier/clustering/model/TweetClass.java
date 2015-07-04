package com.maximgalushka.classifier.clustering.model;

/**
 * @author Maxim Galushka
 */
public enum TweetClass {

  // user published this tweet
  PUBLISHED("published"),

  // user opened full text/clicked on link for tweet
  INTERESTED("interested"),

  // bad quality - ignored
  IGNORED("ignored"),

  // already seen/duplicated
  DUPLICATED("duplicated");

  private String clazz;

  TweetClass(String clazz) {
    this.clazz = clazz;
  }

  public String getClazz() {
    return clazz;
  }
}
