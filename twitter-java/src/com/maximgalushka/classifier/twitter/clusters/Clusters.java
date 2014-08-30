package com.maximgalushka.classifier.twitter.clusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 8/29/2014.
 */
public class Clusters {

    private boolean updated = false;
    private List<Cluster> clusters = new ArrayList<Cluster>();
    private transient Map<Integer, Cluster> reversedIndex;

    public Clusters() {
        this.reversedIndex = new HashMap<Integer, Cluster>();
    }

    public Clusters(boolean updated) {
        this();
        this.updated = updated;
    }

    private void rebuildIndex() {
        reversedIndex.clear();
        for (Cluster c : clusters) {
            reversedIndex.put(c.getTrackingId(), c);
        }
    }

    public boolean isUpdated() {
        return updated;
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

    public void updateCluster(int id, int newId, String label, String message) {
        Cluster update = this.reversedIndex.get(id);
        if (update != null) {
            update.setTrackingId(newId);
            this.reversedIndex.remove(id);
            this.reversedIndex.put(newId, update);
        } else {
            Cluster added = new Cluster(id, label, message);
            clusters.add(added);
            this.reversedIndex.put(id, added);
        }
    }

    public void removeCluster(int id) {
        Cluster c = this.reversedIndex.remove(id);
        this.clusters.remove(c);
    }

    @Override
    public String toString() {
        return clusters.toString();
    }
}
