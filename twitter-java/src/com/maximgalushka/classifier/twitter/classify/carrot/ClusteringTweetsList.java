package com.maximgalushka.classifier.twitter.classify.carrot;

import com.maximgalushka.classifier.twitter.clusters.*;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.*;
import org.carrot2.core.Cluster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.maximgalushka.classifier.twitter.classify.Tools.*;

/**
 * Clustering via Lingo algorithm from Carrot2
 */
public class ClusteringTweetsList {

    public static final Logger log = Logger.getLogger(ClusteringTweetsList.class);

    private List<Cluster> previousClusters;
    private static final ClusteringTweetsList algorithm = new ClusteringTweetsList();

    private AtomicInteger ai = new AtomicInteger(0);
    private final Controller controller;

    private ClusteringTweetsList() {
        // A controller to manage the processing pipeline.
        controller = ControllerFactory.createSimple();
    }

    public static ClusteringTweetsList getAlgorithm() {
        return algorithm;
    }

    /**
     * 1 thread processing because we need to keep previous batch. <br/>
     * <p/>
     * TODO: result of this method should be map:<br>
     * from cluster id: to cluster id
     */
    public synchronized Clusters classify(List<Tweet> batch) throws IOException {
        Clusters result = new Clusters();

        List<Document> docs = readTweetsToDocs(batch);
        if (docs.isEmpty()) return result;


        // Perform clustering by topic using the Lingo algorithm.
        final ProcessingResult byTopicClusters = controller.process(docs, null, LingoClusteringAlgorithm.class);
        final List<Cluster> clustersByTopic = byTopicClusters.getClusters();

        log.debug(String.format("Found [%d] clusters:\n%s", clustersByTopic.size(), printClusters(clustersByTopic)));

        if (previousClusters != null) {
            compareWithPrev(previousClusters, clustersByTopic, result);
        }
        previousClusters = clustersByTopic;
        return result;
    }

    private void compareWithPrev(List<Cluster> prev, List<Cluster> current, Clusters model) {
        // documentId -> Current cluster
        HashMap<String, Cluster> currMap = new HashMap<String, Cluster>();

        // reversed index on cluster id - to find cluster
        HashMap<Integer, Cluster> clusterIdsMap = new HashMap<Integer, Cluster>();

        for (Cluster c : current) {
            clusterIdsMap.put(c.getId(), c);
            for (Document d : c.getAllDocuments()) {
                currMap.put(d.getStringId(), c);
            }
        }

        // threshold to determine if cluster stayed the same.
        // if >= this threshold elements stayed in this cluster - it is considered to stay the same
        double SAME = 0.6d;
        for (Cluster p : prev) {
            List<Document> docs = p.getAllDocuments();
            int totalMoved = 0;
            // next cluster id -> how many documents moved from this cluster to next cluster on next step
            HashMap<Integer, Integer> howManyMovedAndWhere = new HashMap<Integer, Integer>();
            for (Document d : docs) {
                String docId = d.getStringId();
                Cluster toCluster = currMap.get(docId);
                if (toCluster != null) {
                    Integer toClusterId = toCluster.getId();
                    Integer to = howManyMovedAndWhere.get(toClusterId);
                    if (to == null) {
                        howManyMovedAndWhere.put(toClusterId, 0);
                        to = 0;
                    }
                    howManyMovedAndWhere.put(toClusterId, to + 1);
                    totalMoved++;
                }
            }
            boolean split = true;
            for (Integer clusterId : howManyMovedAndWhere.keySet()) {
                int count = howManyMovedAndWhere.get(clusterId);
                double ratio = (double) count / totalMoved;
                Cluster currentCluster = clusterIdsMap.get(clusterId);
                if (ratio >= SAME) {
                    String message = currentCluster.getAllDocuments().get(0).getSummary();
                    model.updateCluster(p.getId(), clusterId, currentCluster.getLabel(), message);
                    log.debug(String.format("Cluster [%s] moved to [%s]", p.getId(), clusterId));
                    split = false;
                    break;
                }
            }
            if (split) {
                model.removeCluster(p.getId());
                log.debug(String.format("Cluster [%s] splitted", p.getId()));
            }
        }
    }

    private List<Document> readTweetsToDocs(List<Tweet> tweets) throws IOException {
        List<Document> docs = new ArrayList<Document>(tweets.size());
        for (Tweet t : tweets) {
            docs.add(new Document(null, t.getText(), null, LanguageCode.ENGLISH, Long.toString(t.getId())));
        }
        return docs;
    }

    private boolean readDocsToDeque(BufferedReader fr, ArrayDeque<Document> docs, int N) throws IOException {
        boolean stop = false;
        String line;
        int count = 0;
        while (count++ < N) {
            line = fr.readLine();
            if (line == null) {
                stop = true;
                break;
            }
            docs.addLast(new Document(null, line, null, LanguageCode.ENGLISH, Integer.toString(ai.incrementAndGet())));
        }
        return stop;
    }

    private String printClusters(List<Cluster> clusters) {
        StringBuilder sb = new StringBuilder();
        for (Cluster c : clusters) {
            sb.append(c.getId()).append(": ").append(c.getLabel()).append("\n");
            for (Document d : c.getAllDocuments()) {
                sb.append("\t").append(d.getStringId()).append("\n");
            }
        }
        return sb.toString();
    }


    public static void main(String[] args) throws IOException {
        log.debug("Start batch clustering");
        BufferedReader fr = new BufferedReader(new FileReader(args[0]));
        ClusteringTweetsList c = new ClusteringTweetsList();

        int D = 1000;
        int delta = 100;
        final ArrayDeque<Document> documents = new ArrayDeque<Document>(D);
        List<Cluster> prev = null;

        // read 1st batch
        boolean stop = c.readDocsToDeque(fr, documents, D);
        int batchId = 1;
        while (true) {
            log.debug(String.format("Start batch [%d]", batchId++));
            // Prepare next batch
            if (!stop) {
                stop = c.readDocsToDeque(fr, documents, delta);
            }

            ArrayList<Document> docs = slice(documents, D);
            if (docs.isEmpty()) break;

            // A controller to manage the processing pipeline.
            final Controller controller = ControllerFactory.createSimple();

            // Perform clustering by topic using the Lingo algorithm.
            final ProcessingResult byTopicClusters = controller.process(docs, null, LingoClusteringAlgorithm.class);
            final List<Cluster> clustersByTopic = byTopicClusters.getClusters();
            log.debug(String.format("Found [%d] clusters:\n%s", clustersByTopic.size(),
                    c.printClusters(clustersByTopic)));

            if (prev != null) {
                c.compareWithPrev(prev, clustersByTopic, new Clusters());
            }
            prev = clustersByTopic;

            if (stop) break;
            cleanFromStart(documents, delta);
        }
    }
}

