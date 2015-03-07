package com.maximgalushka.classifier.clustering.legacy;

import org.tartarus.snowball.ext.EnglishStemmer;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Galushka
 */
@Immutable
@Deprecated
public final class Cluster extends Document {

    private static final boolean DEBUG = true;
    private List<Document> documents = new ArrayList<Document>();

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
        if (DEBUG) {
            documents.add(doc);
        }
        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(doc.getText());
        if (stemmer.stem()) {
            this.getCenter().addText(stemmer.getCurrent());
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Cluster (%d): %s\nAll: %s\n",
                documents.size(),
                super.toString(),
                documents);
    }
}
