package com.maximgalushka.classifier.clustring.model;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
@Immutable
public class Document implements Comparable, Serializable {

    private long id;
    private String text;
    private String author;
    private String url;
    private String image;
    private long timestamp;

    private transient WordCountSparseVector center;

    public Document(long id,
                    String text,
                    String author,
                    String url,
                    String image,
                    long timestamp) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.url = url;
        this.image = image;
        this.timestamp = timestamp;
        this.center = new WordCountSparseVector(this.text);
    }


    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public String getImage() {
        return image;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public WordCountSparseVector getCenter() {
        return center;
    }

    /**
     * @param term term to calculate TF(term, document) for
     * @return TF(term, document) = number of occurrences of term in current document
     */
    public Long tf(String term) {
        return tf(term.hashCode());
    }

    public Long tf(Integer hash) {
        return this.center.getVector().get(hash);
    }

    @Override
    public int compareTo(@Nullable Object what) {
        if (what == null) return 1;
        if (!(what instanceof Document)) {
            throw new IllegalArgumentException(
                    String.format("Cannot compare [%s] to Document", what.getClass())
            );
        }

        Document that = (Document) what;
        return Long.valueOf(that.timestamp).compareTo(this.timestamp);
    }

    @Override
    public String toString() {
        return String.format("[%s]",  text);
    }
}
