package com.maximgalushka.classifier.clustring.realtime;

import com.maximgalushka.classifier.clustring.model.Document;
import com.maximgalushka.classifier.clustring.model.WordCountSparseVector;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public class StatsHolder {

    private volatile AtomicLong documentsCount = new AtomicLong(0);
    private volatile AtomicLong clustersCount = new AtomicLong(0);

    // stores for each word - number of documents containing it
    private WordCountSparseVector globalIdf = new WordCountSparseVector();

    private int clustersToScan = 30;
    //private int clustersCount = 30;
    private float threshold;

    public StatsHolder() {
    }

    public long addDocument(Document document){
        globalIdf.addText(document.getText());
        return documentsCount.incrementAndGet();
    }

    public long getClustersCount() {
        return clustersCount.longValue();
    }

    public long bumpClustersCount() {
        return clustersCount.incrementAndGet();
    }

    public long getDocumentsCount() {
        return documentsCount.get();
    }

    public void setDocumentsCount(long count){}

    public WordCountSparseVector getGlobalVector(){
        return this.globalIdf;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public int getClustersToScan() {
        return clustersToScan;
    }

    public void setClustersToScan(int clustersToScan) {
        this.clustersToScan = clustersToScan;
    }
}
