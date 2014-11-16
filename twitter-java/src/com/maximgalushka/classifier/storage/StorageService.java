package com.maximgalushka.classifier.storage;

import com.maximgalushka.classifier.storage.memcached.MemcachedService;
import com.maximgalushka.classifier.storage.mysql.MysqlService;
import com.maximgalushka.classifier.twitter.clusters.Cluster;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO: crappy design based on singletons.
 *
 * @since 9/16/2014.
 */
@Resource(name = "storage")
public class StorageService {

    public static final Logger log = Logger.getLogger(StorageService.class);

    private static MemcachedService memcached;
    private static MysqlService mysql;

    private static final StorageService service = new StorageService();
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    private StorageService() {
    }

    public static void setMemcached(MemcachedService memcached) {
        StorageService.memcached = memcached;
    }

    public static void setMysql(MysqlService mysql) {
        StorageService.mysql = mysql;
    }

    public static StorageService getService() {
        return service;
    }

    public List<Cluster> findClusters(long delta) {
        // from memory by default
        List<Cluster> results = memcached.mergeFromTimestamp(delta);
        if (results.isEmpty()) {
            log.warn("Memcached is empty - loading clusters from database");
            HashMap<Long, Clusters> fromdb = mysql.loadClusters(delta);
            for (long ts : fromdb.keySet()) {
                Clusters cls = fromdb.get(ts);
                try {
                    memcached.saveClustersGroup(ts, cls);
                } catch (Exception e) {
                    log.error(String.format("Cannot save clusters group for: [%d]: %s", ts, e));
                }
            }
            // try once more after refresh from DB
            return memcached.mergeFromTimestamp(delta);
        }
        return results;
    }

    public void saveNewClustersGroup(final Clusters group) {
        long timestamp = 0L;
        try {
            // save in memcached
            timestamp = memcached.createNewClustersGroup(group);
        } catch (Exception e) {
            log.error(String.format("Cannot save new clusters group: %s", e));
        }

        // save in db async way
        final long finalTimestamp = timestamp;
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if(finalTimestamp != 0L) {
                    mysql.saveNewClustersGroup(finalTimestamp, group);
                }
                else{
                    log.error(String.format("Error happened during saving in group memcached, timestamp == 0. Skipping mysql save."));
                }
            }
        });
    }
}
