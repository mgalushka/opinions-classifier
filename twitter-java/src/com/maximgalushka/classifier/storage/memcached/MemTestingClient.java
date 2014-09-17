package com.maximgalushka.classifier.storage.memcached;

import com.maximgalushka.classifier.twitter.clusters.Cluster;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * @author Maxim Galushka
 */
public class MemTestingClient {

    public static final Logger log = Logger.getLogger(MemTestingClient.class);

    public static void main(String[] args) throws Exception {
        MemcachedService ss = MemcachedService.getService();
        Clusters add = new Clusters();
        add.addClusters(Arrays.asList(new Cluster(1, "test", "some message", 70, "http://google.com", "http://imgur.com")));
        long latest = ss.createNewClustersGroup(add);
        log.debug(String.format("Added new clusters group for [%d]", latest));
        log.debug(ss.mergeFromTimestamp(30 * 60 * 1000));
    }
}
