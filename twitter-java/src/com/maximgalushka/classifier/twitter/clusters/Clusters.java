package com.maximgalushka.classifier.twitter.clusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 8/29/2014.
 */
public class Clusters {

    private int size;
    private List<Cluster> clusters = new ArrayList<Cluster>();
    private transient Map<Integer, Cluster> reversedIndex;

    public Clusters() {
        this.reversedIndex = new HashMap<Integer, Cluster>();
    }

    private void rebuildIndex() {
        reversedIndex.clear();
        for (Cluster c : clusters) {
            reversedIndex.put(c.getTrackingId(), c);
        }
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void cleanClusters() {
        this.clusters.clear();
        rebuildIndex();
    }

    public void addClusters(List<Cluster> clusters) {
        this.clusters.addAll(clusters);
        rebuildIndex();
    }

    public Cluster clusterById(int id) {
        return this.reversedIndex.get(id);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return clusters.toString();
    }
}
