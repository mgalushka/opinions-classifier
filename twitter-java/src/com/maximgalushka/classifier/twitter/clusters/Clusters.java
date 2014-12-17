package com.maximgalushka.classifier.twitter.clusters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 8/29/2014.
 */
@SuppressWarnings("UnusedDeclaration")
public class Clusters implements Serializable {

    private static final long serialVersionUID = -5519589416029255789L;

    private List<TweetsCluster> clusters = new ArrayList<TweetsCluster>();

    private transient Map<Integer, TweetsCluster> reversedIndex;

    public Clusters() {
        this.reversedIndex = new HashMap<Integer, TweetsCluster>();
    }

    private void rebuildIndex() {
        reversedIndex.clear();
        for (TweetsCluster c : clusters) {
            reversedIndex.put(c.getTrackingId(), c);
        }
    }

    public List<TweetsCluster> getClusters() {
        return clusters;
    }

    public void cleanClusters() {
        this.clusters.clear();
        rebuildIndex();
    }

    public void addClusters(List<TweetsCluster> clusters) {
        this.clusters.addAll(clusters);
        rebuildIndex();
    }

    // TODO: looks like crap
    public void addClustersNoIndex(List<TweetsCluster> clusters) {
        this.clusters.addAll(clusters);
    }

    public TweetsCluster clusterById(int id) {
        return this.reversedIndex.get(id);
    }

    @Override
    public String toString() {
        return clusters.toString();
    }
}
