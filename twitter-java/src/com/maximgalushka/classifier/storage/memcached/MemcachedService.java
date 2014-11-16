package com.maximgalushka.classifier.storage.memcached;

import com.google.common.collect.Lists;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.clusters.Cluster;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import net.spy.memcached.CASMutation;
import net.spy.memcached.CASMutator;
import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Memcached is designed to store all the latest clusters generated sequentially for latest 24 hours.<br/>
 * For older data - implement search in MySQL
 *
 * @since 9/12/2014.
 */
@Resource(name = "memcached")
public class MemcachedService {

    public static final Logger log = Logger.getLogger(MemcachedService.class);

    private static final int HOURS24 = 24 * 60 * 60 * 1000;

    private static final String TIMESTAMPS_KEY = "timestamps.all.key";

    private static final ArrayDequeTranscoder ARRAY_DEQUE_LONG_TRANSCODER = new ArrayDequeTranscoder();
    private static final ClustersTranscoder CLUSTERS_TRANSCODER = new ClustersTranscoder();

    private LocalSettings settings;

    private MemcachedClient memcached;

    public MemcachedService() {
    }

    public void setSettings(LocalSettings settings) {
        this.settings = settings;
    }

    @PostConstruct
    private void init() throws IOException {
        this.memcached = new MemcachedClient(
                new InetSocketAddress(
                        this.settings.value(LocalSettings.MEMCACHED_HOST),
                        Integer.valueOf(
                                this.settings.value(LocalSettings.MEMCACHED_PORT))));
    }

    /**
     * @return list of timestamps which are stored in cache and which we have data for.
     */
    @SuppressWarnings("unchecked")
    public ArrayDeque<Long> listTimestamps() {
        return this.memcached.get(TIMESTAMPS_KEY, ARRAY_DEQUE_LONG_TRANSCODER);
    }

    /**
     * Inserts (at start of queue) latest timestamp.<br/>
     *
     * @return inserted timestamp
     * @throws Exception if any
     */
    public long addTimestamp() throws Exception {
        // current timestamp
        final long timestamp = new Date().getTime();

        // This is how we modify a list when we find one in the cache.
        CASMutation<ArrayDeque<Long>> mutation = new CASMutation<ArrayDeque<Long>>() {
            // This is only invoked when a value actually exists.
            @Override
            public ArrayDeque<Long> getNewValue(ArrayDeque<Long> current) {
                // Not strictly necessary if you specify the storage as
                // LinkedList (our initial value isn't), but I like to keep
                // things functional anyway, so I'm going to copy this list
                // first.
                ArrayDeque<Long> copy = new ArrayDeque<Long>(current);

                // Remove from the end (oldest) of list all which are older then required time span
                for (Iterator<Long> last = current.descendingIterator(); last.hasNext(); ) {
                    long oldest = last.next();
                    if ((timestamp - oldest) >= HOURS24) {
                        last.remove();
                    } else {
                        break;
                    }
                }
                // Add latest timestamp to the head of the list
                copy.addFirst(timestamp);
                return copy;
            }
        };

        // The initial value -- only used when there's no list stored under
        // the key.
        ArrayDeque<Long> initialValue = new ArrayDeque<Long>();
        initialValue.addFirst(timestamp);

        // The mutator who'll do all the low-level stuff.
        CASMutator<ArrayDeque<Long>> mutator = new CASMutator<ArrayDeque<Long>>(this.memcached, ARRAY_DEQUE_LONG_TRANSCODER);

        // This returns whatever value was successfully stored within the
        // cache -- either the initial list as above, or a mutated existing
        // one
        mutator.cas(TIMESTAMPS_KEY, initialValue, 0, mutation);

        // return inserted timestamp
        return timestamp;
    }

    /**
     * @param group clusters group to be stored in memcached, timestamp - to be generated
     */
    public long createNewClustersGroup(Clusters group) throws Exception {
        // create timestamp
        long timestamp = addTimestamp();

        // return inserted timestamp
        return saveClustersGroup(timestamp, group);
    }

    public long saveClustersGroup(long timestamp, Clusters group) throws Exception {
        // This is how we modify a list when we find one in the cache.
        CASMutation<Clusters> mutation = new CASMutation<Clusters>() {
            // This is only invoked when a value actually exists.
            @Override
            public Clusters getNewValue(Clusters current) {
                return current;
            }
        };

        // The mutator who'll do all the low-level stuff.
        CASMutator<Clusters> mutator = new CASMutator<Clusters>(this.memcached, CLUSTERS_TRANSCODER);

        // This returns whatever value was successfully stored within the
        // cache -- either the initial list as above, or a mutated existing
        // one
        String key = Long.toString(timestamp);
        mutator.cas(key, group, 0, mutation);

        // return inserted timestamp
        return timestamp;
    }

    /**
     * @param delta period in millis to merge clusters for (since current timestamp)
     */
    public List<Cluster> mergeFromTimestamp(long delta) {
        ArrayDeque<Long> timestamps = memcached.get(TIMESTAMPS_KEY, ARRAY_DEQUE_LONG_TRANSCODER);
        long now = new Date().getTime();
        LinkedHashMap<Cluster, Integer> merged = new LinkedHashMap<Cluster, Integer>(64, 0.75f, false);
        for (Iterator<Long> last = timestamps.descendingIterator(); last.hasNext(); ) {
            long oldest = last.next();
            if ((now - oldest) < delta) {
                String key = Long.toString(oldest);
                Clusters group = memcached.get(key, CLUSTERS_TRANSCODER);
                if (group != null) {
                    List<Cluster> clusters = group.getClusters();
                    for (Cluster c : clusters) {
                        merged.put(c, c.getId());
                    }
                } else {
                    log.warn(String.format("Not found clusters for key: [%s]", key));
                }
            }
        }
        return Lists.newArrayList(merged.keySet());
    }
}
