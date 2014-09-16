package com.maximgalushka.classifier.storage.mysql;

import com.maximgalushka.classifier.twitter.clusters.Cluster;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

public class MysqlIntegrationServiceTest {

    @Test
    public void testLoadClusters() throws Exception {
        MysqlService ms = MysqlService.getService();
        Assert.assertFalse(ms.loadClusters(20000).isEmpty());
    }

    @Test
    public void testSaveNewClustersGroup() throws Exception {
        MysqlService ms = MysqlService.getService();
        long timestamp = new Date().getTime();
        Clusters add = new Clusters();
        add.addClusters(Arrays.asList(new Cluster(1, "test00001", "test", 70, "http://google222.com", "http://imgur333.com")));
        ms.saveNewClustersGroup(timestamp, add);
        Assert.assertFalse(ms.loadClusters(20000).isEmpty());
    }
}