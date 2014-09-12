package com.maximgalushka.classifier.storage.memcached;

import com.maximgalushka.classifier.twitter.clusters.Cluster;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;


/**
 * Memcached is designed to store all the latest clusters generated sequentially for latest 24 hours.<br/>
 * For older data - implement search in MySQL
 *
 * @since 9/12/2014.
 */
public class StorageService {

    private static final StorageService service = new StorageService();
    private static final int HOURS24 = 24 * 60 * 60 * 1000;

    private MemcachedClient memcached;

    private StorageService() {
        try {
            // TODO: move server URL to config
            this.memcached = new MemcachedClient(
                    new InetSocketAddress("ec2-54-68-39-246.us-west-2.compute.amazonaws.com", 11211));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StorageService getService() {
        return service;
    }

    /**
     * @param cluster cluster to store in memcached
     */
    public void newCluster(Cluster cluster) {
        memcached.add(Integer.toString(cluster.getId()), HOURS24, cluster);
    }

    /**
     * @param cluster cluster to touch in memcached
     */
    public void touchCluster(Cluster cluster) {

    }

    public List<Cluster> all() {
        //memcached.get
        return null;
    }
}
