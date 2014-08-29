package com.maximgalushka.classifier.twitter.clusters;

/**
 * Operations on the cluster.
 * stay - manes that cluster was not splitted during last operation of labelling, so stays the same
 * remove - cluster dissolves and should be removed from front-end
 *
 * @since 8/29/2014.
 */
public enum ClusterOperation {

    STAY("stay"),
    REMOVE("remove");

    private String ops;

    ClusterOperation(String _ops) {
        this.ops = _ops;
    }

    public String getCode() {
        return this.ops;
    }
}
