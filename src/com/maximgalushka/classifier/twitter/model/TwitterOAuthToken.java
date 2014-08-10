package com.maximgalushka.classifier.twitter.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Maxim Galushka
 */
public class TwitterOAuthToken {

    @SerializedName("access_token")
    private String accessToken;

    public TwitterOAuthToken() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
