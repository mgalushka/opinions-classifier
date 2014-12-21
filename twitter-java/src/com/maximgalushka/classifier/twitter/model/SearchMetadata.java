package com.maximgalushka.classifier.twitter.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Maxim Galushka
 */
public class SearchMetadata {

  @SerializedName("next_results")
  private String nextResults;

  @SerializedName("max_id")
  private long maxId;

  public SearchMetadata() {
  }

  public String getNextResults() {
    return nextResults;
  }

  public void setNextResults(String nextResults) {
    this.nextResults = nextResults;
  }

  public long getNextSinceId(){
    int start = nextResults.indexOf("max_id=") + 7;
    int end = nextResults.indexOf("&", start);
    return Long.parseLong(this.nextResults.substring(start, end));
  }

  public long getMaxId() {
    return maxId;
  }

  public void setMaxId(long maxId) {
    this.maxId = maxId;
  }
}
