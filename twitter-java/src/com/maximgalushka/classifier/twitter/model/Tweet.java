package com.maximgalushka.classifier.twitter.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Maxim Galushka
 */
public class Tweet {

    private long id;
    private String text;

    @SerializedName("user")
    private User author;
    private boolean retweeted;

    public Tweet() {
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

    @Override
    public String toString() {
        return String.format("[%d, '%s', %s]", id, text, author);
    }
}
