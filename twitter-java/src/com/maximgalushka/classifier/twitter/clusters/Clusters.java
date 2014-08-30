package com.maximgalushka.classifier.twitter.clusters;

import com.google.gson.annotations.SerializedName;
import com.maximgalushka.classifier.twitter.model.Tweet;

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
            reversedIndex.put(c.getId(), c);
        }
    }

    public boolean isUpdated() {
        return updated;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void cleanTweets() {
        clusters.clear();
    }

    public void addClusters(List<Cluster> clusters) {
        this.clusters.addAll(clusters);
        rebuildIndex();
    }

    public void updateCluster(int id, int newId, String label, String message, ClusterOperation ops) {
        Cluster update = this.reversedIndex.get(id);
        if (update != null) {
            // TODO: somehow untouched clusters which disappeared should be completely removed from list
            // TODO: or it will grow infinitely
            update.setOperation(ops);
            update.setId(newId);
            this.reversedIndex.remove(id);
            this.reversedIndex.put(newId, update);
        } else {
            Cluster added = new Cluster(id, label, message, ops);
            clusters.add(added);
            this.reversedIndex.put(id, added);
        }
    }

    @Override
    public String toString() {
        return clusters.toString();
    }
}
