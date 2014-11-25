package com.maximgalushka.classifier.clustring.model;

import org.tartarus.snowball.ext.EnglishStemmer;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * @author Maxim Galushka
 */
@Immutable
public class Document implements Comparable, Serializable {

    private long id;
    private String text;
    private String author;
    private String url;
    private String image;
    private long timestamp;

    private transient IdfSparseVector center;

    protected Document() {

    }

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

        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(text);
        if (stemmer.stem()) {
            this.center = new IdfSparseVector(stemmer.getCurrent());
        } else {
            throw new RuntimeException("Cannot stem input text");
        }
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

    public IdfSparseVector getCenter() {
        return center;
    }

    @Override
    public int compareTo(Object what) {
        if (what == null) return 1;
        if (!(what instanceof Document)) {
            throw new IllegalArgumentException(
                    String.format("Cannot compare [%s] to Document", what.getClass())
            );
        }

        Document that = (Document) what;
        return Long.valueOf(that.timestamp).compareTo(this.timestamp);
    }
}
