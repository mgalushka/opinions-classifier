package com.maximgalushka.classifier.clustring.model;

import org.tartarus.snowball.ext.EnglishStemmer;

import javax.annotation.concurrent.Immutable;

/**
 * @author Maxim Galushka
 */
@Immutable
public final class Cluster extends Document {

    public Cluster(Document d) {
        super(d.getId(), d.getText(), d.getAuthor(),
                d.getUrl(), d.getImage(), d.getTimestamp());
    }

    /**
     * Adds document to cluster (recalculates its vector)
     *
     * @param doc document to add
     */
    public synchronized void addDocument(Document doc) {
        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(doc.getText());
        if (stemmer.stem()) {
            this.getCenter().addText(stemmer.getCurrent());
        }
    }
}
