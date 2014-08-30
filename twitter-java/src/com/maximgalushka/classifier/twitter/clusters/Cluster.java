package com.maximgalushka.classifier.twitter.clusters;

/**
 * @since 8/29/2014.
 */
public class Cluster {

    /**
     * Original cluster id - how it is displayed on GUI
     */
    private int id;

    /**
     * Current cluster id according Lingo algorithm - also cluster can be found in reversed index by this id
     */
    private transient int trackingId;

    private String label;
    private transient ClusterOperation operation;
    private String message;

    public Cluster(int id, String label, String message) {
        this.id = id;
        // at creation both id and trackingId are the same
        this.trackingId = id;
        this.label = label;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(int trackingId) {
        this.trackingId = trackingId;
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
