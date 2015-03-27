package com.maximgalushka.classifier.twitter.model;

/**
 * Class which is designed to be used as storage for a tweet
 *
 * @since 9/11/2014.
 */
public class TweetTextWrapper {

  private String text;
  private Tweet tweet;

  public TweetTextWrapper(String text, Tweet tweet) {
    this.text = text;
    this.tweet = tweet;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Tweet getTweet() {
    return tweet;
  }

  public void setTweet(Tweet tweet) {
    this.tweet = tweet;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TweetTextWrapper that = (TweetTextWrapper) o;

    if (text != null ? !text.equals(that.text) : that.text != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return text != null ? text.hashCode() : 0;
  }
}
