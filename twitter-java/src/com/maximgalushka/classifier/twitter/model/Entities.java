package com.maximgalushka.classifier.twitter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 9/2/2014.
 */
@SuppressWarnings("UnusedDeclaration")
public class Entities {

    private List<Urls> urls = new ArrayList<Urls>();
    private List<Media> media = new ArrayList<Media>();

    public Entities() {
    }

    public List<Urls> getUrls() {
        return urls;
    }

    public void setUrls(List<Urls> urls) {
        this.urls = urls;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }
}
