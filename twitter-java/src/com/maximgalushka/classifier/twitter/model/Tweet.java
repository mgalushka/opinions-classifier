package com.maximgalushka.classifier.twitter.model;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

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
  private transient long accountId;

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

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  public long getAccountId() {
    return accountId;
  }

  public static Tweet fromStatus(Status status) {
    Tweet tweet = new Tweet(status.getId(), status.getText());
    tweet.setAccountId(status.getUser().getId());

    User author = new User();
    author.setScreenName(status.getUser().getScreenName());
    author.setName(status.getUser().getName());
    tweet.setAuthor(author);

    Entities entities = new Entities();

    Urls urls = new Urls();
    for (URLEntity urlEnt : status.getURLEntities()) {
      entities.getUrls().add(new Urls(urlEnt.getDisplayURL()));
    }

    Media media = new Media();
    for (MediaEntity mediaEnt : status.getMediaEntities()) {
      entities.getMedia().add(new Media(mediaEnt.getDisplayURL()));
    }

    tweet.setEntities(entities);
    tweet.setRetweeted(status.isRetweet());
    tweet.setRetweetCount(status.getRetweetCount());
    tweet.setFavouriteCount(status.getFavoriteCount());

    return tweet;
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
        "[%d, %d, '%s', %s, %s, %s]",
        id,
        accountId,
        text,
        author,
        url,
        image
      );
    }
    return String.format("[%d, '%s', %s]", id, text, author);
  }
}
