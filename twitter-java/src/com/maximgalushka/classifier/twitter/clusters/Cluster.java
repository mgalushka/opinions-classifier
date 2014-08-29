package com.maximgalushka.classifier.twitter.clusters;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 8/29/2014.
 */
public class Cluster {

    private int id;
    private String label;
    private ClusterOperation operation;
    private String message;

    public Cluster(int id, String label, String message, ClusterOperation operation) {
        this.id = id;
        this.label = label;
        this.message = message;
        this.operation = operation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ClusterOperation getOperation() {
        return operation;
    }

    public void setOperation(ClusterOperation operation) {
        this.operation = operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return String.format("[%d, %s: [%s]", id, label, message);
    }

}
