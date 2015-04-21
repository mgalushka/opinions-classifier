package com.maximgalushka.classifier.twitter.model;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public class Tweet {

  private long id;
  private String text;

  @SerializedName("user")
  private User author;
  private boolean retweeted;

  private Entities entities;

  @SerializedName("favorite_count")
  private int favouriteCount;

  @SerializedName("retweet_count")
  private int retweetCount;

  private transient boolean excluded;
  private transient String excludedReason;

  public Tweet() {
  }

  public Tweet(long id, String text) {
    this.id = id;
    this.text = text;
  }

  public Tweet(String text) {
    this.text = text;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public boolean isRetweeted() {
    return retweeted;
  }

  public void setRetweeted(boolean retweeted) {
    this.retweeted = retweeted;
  }

  public int getFavouriteCount() {
    return favouriteCount;
  }

  public void setFavouriteCount(int favouriteCount) {
    this.favouriteCount = favouriteCount;
  }

  public boolean isExcluded() {
    return excluded;
  }

  public void setExcluded(boolean excluded) {
    this.excluded = excluded;
  }

  public String getExcludedReason() {
    return StringUtils.isBlank(excludedReason) ? "" : excludedReason;
  }

  public void setExcludedReason(String excludedReason) {
    this.excludedReason = excludedReason;
  }

  public int getRetweetCount() {
    return retweetCount;
  }

  public void setRetweetCount(int retweetCount) {
    this.retweetCount = retweetCount;
  }

  public Entities getEntities() {
    return entities;
  }

  public void setEntities(Entities entities) {
    this.entities = entities;
  }

  @Override
  public String toString() {
    Entities e = this.getEntities();
    if (e != null) {
      String url = CollectionUtils.isEmpty(e.getUrls()) ? "" : e.getUrls()
                                                                .get(0)
                                                                .getUrl();
      String image = CollectionUtils.isEmpty(e.getMedia()) ? "" : e.getMedia()
                                                                   .get(0)
                                                                   .getUrl();
      return String.format(
        "[%d, '%s', %s, %s, %s]",
        id,
        text,
        author,
        url,
        image
      );
    }
    return String.format("[%d, '%s', %s]", id, text, author);
  }
}
