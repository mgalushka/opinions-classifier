package com.maximgalushka.classifier.twitter.clusters;

/**
 * @since 8/29/2014.
 */
@SuppressWarnings("UnusedDeclaration")
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
    private String message;
    private String url;
    private String image;

    private int score;

    public Cluster(int id, String label, String message, int score, String url, String image) {
        this.id = id;
        // at creation both id and trackingId are the same
        this.trackingId = id;
        this.label = label;
        this.message = message;
        this.score = score;
        this.url = url;
        this.image = image;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String toString() {
        return String.format("[%d, %s: [%s]", id, label, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cluster cluster = (Cluster) o;

        if (message != null && cluster.message != null && message.equals(cluster.message)) return true;
        if (url != null && cluster.url != null && url.equals(cluster.url)) return true;
        if (image != null && cluster.image != null && image.equals(cluster.image)) return true;

        return false;
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }

    @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "CloneDoesntCallSuperClone"})
    @Override
    public Cluster clone() {
        Cluster copy = new Cluster(id, label, message, score, url, image);
        copy.setTrackingId(trackingId);
        return copy;
    }
}
