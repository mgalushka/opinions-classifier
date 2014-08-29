package com.maximgalushka.classifier.twitter.clusters;

import com.google.gson.annotations.SerializedName;
import com.maximgalushka.classifier.twitter.model.Tweet;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 8/29/2014.
 */
public class Clusters {

    private boolean updated = false;
    private List<Cluster> clusters = new ArrayList<Cluster>();

    public Clusters() {
    }

    public Clusters(boolean updated) {
        this.updated = updated;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public void cleanTweets() {
        clusters.clear();
    }

    public void addClusters(List<Cluster> clusters) {
        this.clusters.addAll(clusters);
    }

    @Override
    public String toString() {
        return clusters.toString();
    }
}
