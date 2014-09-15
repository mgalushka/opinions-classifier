package com.maximgalushka.classifier.storage.memcached;

import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.clusters.Cluster;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * @author Maxim Galushka
 */
public class MemClient {

    public static final Logger log = Logger.getLogger(MemClient.class);

    private static final LocalSettings settings = LocalSettings.settings();

    public static void main(String[] args) throws Exception {
        StorageService ss = StorageService.getService();
        Clusters add = new Clusters();
        add.addClusters(Arrays.asList(new Cluster(1, "test", "some message", 70, "http://google.com", "http://imgur.com")));
        long latest = ss.addNewClustersGroup(add);
        log.debug(String.format("Added new clusters group for [%d]", latest));
        log.debug(ss.mergeFromTimestamp(latest, 1));
    }
}
