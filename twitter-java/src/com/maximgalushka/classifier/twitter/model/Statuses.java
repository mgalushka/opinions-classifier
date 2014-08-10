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

    public Statuses() {
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }
}
