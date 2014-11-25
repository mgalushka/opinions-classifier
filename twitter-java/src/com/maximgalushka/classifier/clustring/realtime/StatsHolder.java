package com.maximgalushka.classifier.clustring.realtime;

import com.maximgalushka.classifier.clustring.model.Document;
import com.maximgalushka.classifier.clustring.model.IdfSparseVector;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Maxim Galushka
 */
public class StatsHolder {

    private volatile AtomicLong documentsCount = new AtomicLong(0);
    private IdfSparseVector globalIdf = new IdfSparseVector();

    private int clustersToScan = 30;
    private int clustersCount = 30;
    private float threshold;

    public StatsHolder() {
    }

    public long addDocument(Document document){
        globalIdf.addText(document.getText());
        return documentsCount.incrementAndGet();
    }

    public int getClustersCount() {
        return clustersCount;
    }

    public void setClustersCount(int clustersCount) {}

    public long getDocumentsCount() {
        return documentsCount.get();
    }

    public void setDocumentsCount(long count){}

    public IdfSparseVector getGlobalVector(){
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
