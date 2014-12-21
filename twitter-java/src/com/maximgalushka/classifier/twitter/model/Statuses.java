package com.maximgalushka.classifier.twitter.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Galushka
 */
public class Statuses {

    @SerializedName("statuses")
    private List<Tweet> tweets = new ArrayList<Tweet>();

    @SerializedName("search_metadata")
    private SearchMetadata metadata;

    public Statuses() {
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }

    public SearchMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SearchMetadata metadata) {
        this.metadata = metadata;
    }
}
