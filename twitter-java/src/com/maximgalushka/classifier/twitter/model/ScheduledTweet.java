package com.maximgalushka.classifier.twitter.model;

import javax.annotation.concurrent.NotThreadSafe;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
@NotThreadSafe
public final class ScheduledTweet {

  private Tweet data;
  private boolean retweet;
  private Date scheduled;

  public ScheduledTweet(Tweet data, boolean retweet) {
    this.data = data;
    this.retweet = retweet;
  }

  public Tweet getData() {
    return data;
  }

  public boolean isRetweet() {
    return retweet;
  }

  public Date getScheduled() {
    return scheduled;
  }

  public void setScheduled(Date scheduled) {
    this.scheduled = scheduled;
  }

  private static final SimpleDateFormat SDF =
    new SimpleDateFormat("yyyy-MM-dd HH:mm");

  @Override
  public String toString() {
    return String.format(
      "[%s][%s][%s]\n",
      retweet ? "retweet" : "new",
      (scheduled == null) ? "unscheduled" : SDF.format(scheduled),
      data.getText()
    );
  }
}
