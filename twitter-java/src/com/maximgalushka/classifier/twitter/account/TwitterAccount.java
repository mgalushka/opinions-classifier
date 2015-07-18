package com.maximgalushka.classifier.twitter.account;

/**
 * @author Maxim Galushka
 */
public class TwitterAccount {

  private long id;
  private String account;
  private String consumerKey;
  private String consumerSecret;
  private String accessToken;
  private String accessTokenSecret;

  String language;
  String terms;

  private String blacklist;
  private String usersBlacklist;

  public TwitterAccount() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getConsumerKey() {
    return consumerKey;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public String getConsumerSecret() {
    return consumerSecret;
  }

  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAccessTokenSecret() {
    return accessTokenSecret;
  }

  public void setAccessTokenSecret(String accessTokenSecret) {
    this.accessTokenSecret = accessTokenSecret;
  }

  public String getBlacklist() {
    return blacklist;
  }

  public void setBlacklist(String blacklist) {
    this.blacklist = blacklist;
  }

  public String getUsersBlacklist() {
    return usersBlacklist;
  }

  public void setUsersBlacklist(String usersBlacklist) {
    this.usersBlacklist = usersBlacklist;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getTerms() {
    return terms;
  }

  public void setTerms(String terms) {
    this.terms = terms;
  }
}
