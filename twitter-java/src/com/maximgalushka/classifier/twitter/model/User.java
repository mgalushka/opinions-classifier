package com.maximgalushka.classifier.twitter.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Maxim Galushka
 */
public class User {

    @SerializedName("screen_name")
    private String screenName;

    @SerializedName("name")
    private String name;

    public User() {
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("author = [@%s, '%s']", screenName, name);
    }
}
