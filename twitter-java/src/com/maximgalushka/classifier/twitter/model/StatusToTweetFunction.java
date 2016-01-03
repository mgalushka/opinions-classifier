package com.maximgalushka.classifier.twitter.model;

import com.google.common.base.Function;
import twitter4j.Status;

import javax.annotation.Nullable;

/**
 * @author Maxim Galushka
 */
public final class StatusToTweetFunction implements Function<Status, Tweet>{

  @Nullable
  @Override
  public Tweet apply(Status status) {
    return Tweet.fromStatus(status);
  }
}
