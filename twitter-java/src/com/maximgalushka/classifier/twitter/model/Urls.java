package com.maximgalushka.classifier.twitter.model;

import com.google.gson.annotations.SerializedName;

/**
 * @since 9/2/2014.
 */
@SuppressWarnings("UnusedDeclaration")
public class Urls {

    @SerializedName("expanded_url")
    private String url;

    public Urls() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
