package com.maximgalushka.classifier.storage.memcached;

import com.maximgalushka.classifier.twitter.clusters.Cluster;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.Assert;

import java.util.Arrays;

/**
 * Test is using real data in memcached.
 */
public class StorageServiceIntegrationTest {

    public static final Logger log = Logger.getLogger(StorageServiceIntegrationTest.class);
    public static final int DELTA = 24 * 60 * 60 * 1000;

    @Test
    public void testGetService() throws Exception {
        StorageService ss = StorageService.getService();
        Assert.assertNotNull(ss);
    }

    @Test
    public void testListTimestamps() throws Exception {
        StorageService ss = StorageService.getService();
        Assert.assertFalse(ss.listTimestamps().isEmpty());
    }

    @Test
    public void testAddTimestamp() throws Exception {
        StorageService ss = StorageService.getService();
        log.debug(String.format("Timestamps queue: [%s]", ss.addTimestamp()));
        Assert.assertFalse(ss.listTimestamps().isEmpty());
    }

    @Test
    public void testAddNewClustersGroup() throws Exception {
        StorageService ss = StorageService.getService();
        Clusters add = new Clusters();
        add.addClusters(Arrays.asList(new Cluster(1, "test", "some message", 70, "http://google.com", "http://imgur.com")));
        long latest = ss.addNewClustersGroup(add);
        log.debug(String.format("Added new clusters group for [%d]", latest));
        Assert.assertFalse(ss.mergeFromTimestamp(DELTA).isEmpty());
    }

    @Test
    public void testMergeFromTimestamp() throws Exception {
        StorageService ss = StorageService.getService();
        log.debug(String.format("Clusters from timestamp: %s", ss.mergeFromTimestamp(DELTA)));
    }
}