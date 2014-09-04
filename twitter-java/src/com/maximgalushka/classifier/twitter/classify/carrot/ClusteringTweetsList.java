package com.maximgalushka.classifier.twitter.classify.carrot;

import com.google.common.base.Optional;
import com.maximgalushka.classifier.twitter.clusters.*;
import com.maximgalushka.classifier.twitter.model.Entities;
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
     * TODO: result of this method should be map:<br/>
     * from cluster id: to cluster id<br/>
     * <p/>
     * This method updates underlying model.<br/>
     * TODO: maybe this is not the best design. To think about it.<br/>
     */
    public synchronized void classify(List<Tweet> batch, Clusters model) throws IOException {
        List<Document> docs = readTweetsToDocs(batch);
        if (docs.isEmpty()) return;

        // helper map to extract any required tween metadata
        Map<String, Tweet> tweetsIndex = readTweetsToMap(batch);

        // Perform clustering by topic using the Lingo algorithm.
        final ProcessingResult byTopicClusters = controller.process(docs, null, LingoClusteringAlgorithm.class);
        final List<Cluster> clustersByTopic = byTopicClusters.getClusters();

        log.debug(String.format("Found [%d] clusters:\n%s", clustersByTopic.size(), printClusters(clustersByTopic)));

        if (previousClusters != null) {
            Map<Integer, Optional<Integer>> fromTo = compareWithPrev(previousClusters, clustersByTopic);
            updateModel(model, clustersByTopic, fromTo, tweetsIndex);
        }
        previousClusters = clustersByTopic;
    }

    private void updateModel(final Clusters clusters, List<Cluster> currentClusters,
                             Map<Integer, Optional<Integer>> fromTo, Map<String, Tweet> tweetsIndex) {
        // if cluster id is no longer in fromTo map - we should remove it
        // if cluster migrated to another - we should change it tracking id
        // if new cluster was created - we should just add it
        Map<Integer, Cluster> currentClustersIndex = new HashMap<Integer, Cluster>();
        for (Cluster c : currentClusters) currentClustersIndex.put(c.getId(), c);

        List<com.maximgalushka.classifier.twitter.clusters.Cluster> updated = new ArrayList<com.maximgalushka.classifier.twitter.clusters.Cluster>();
        List<com.maximgalushka.classifier.twitter.clusters.Cluster> snapshot = Collections.unmodifiableList(clusters.getClusters());
        for (com.maximgalushka.classifier.twitter.clusters.Cluster old : snapshot) {
            Optional<Integer> to = Optional.fromNullable(fromTo.get(old.getId())).or(Optional.<Integer>absent());
            if (to.isPresent()) {
                com.maximgalushka.classifier.twitter.clusters.Cluster updatedCluster = old.clone();
                Cluster newCluster = currentClustersIndex.get(to.get());
                updatedCluster.setTrackingId(to.get());
                updatedCluster.setScore(old.getScore() + newCluster.getAllDocuments().size());
                updated.add(updatedCluster);
            }
        }
        for (Cluster current : currentClusters) {
            com.maximgalushka.classifier.twitter.clusters.Cluster old = clusters.clusterById(current.getId());
            if (old == null) {
                Tweet representative = findRepresentative(current.getAllDocuments(), tweetsIndex);
                Entities entities = representative.getEntities();
                String url = "";
                String image = "";
                if (entities != null) {
                    url = entities.getUrls().isEmpty() ? "" : entities.getUrls().get(0).getUrl();
                    image = entities.getMedia().isEmpty() ? "" : entities.getMedia().get(0).getUrl();
                }
                // create new
                updated.add(new com.maximgalushka.classifier.twitter.clusters.Cluster(
                        current.getId(),
                        current.getLabel(),
                        representative.getText(),
                        current.getAllDocuments().size(),
                        url, image));
            }
        }
        int size = 0;
        for (com.maximgalushka.classifier.twitter.clusters.Cluster c : updated) {
            size += c.getScore();
        }
        clusters.setSize(size);
        synchronized (this) {
            clusters.cleanClusters();
            List<com.maximgalushka.classifier.twitter.clusters.Cluster> finalList = filterDuplicateRepresentations(updated);
            log.debug(String.format("Final clusters list: [%s]", finalList));
            clusters.addClusters(finalList);
        }
    }

    /**
     * TODO: kind of messy method to filter out duplicate cluster representations if any.
     *
     * @return list of clusters (domain model) without duplicated
     */
    private List<com.maximgalushka.classifier.twitter.clusters.Cluster>
    filterDuplicateRepresentations(List<com.maximgalushka.classifier.twitter.clusters.Cluster> clusters) {
        List<com.maximgalushka.classifier.twitter.clusters.Cluster> result
                = new ArrayList<com.maximgalushka.classifier.twitter.clusters.Cluster>();

        HashMap<String, com.maximgalushka.classifier.twitter.clusters.Cluster> messagesIndex =
                new HashMap<String, com.maximgalushka.classifier.twitter.clusters.Cluster>();

        HashMap<String, com.maximgalushka.classifier.twitter.clusters.Cluster> urlsIndex =
                new HashMap<String, com.maximgalushka.classifier.twitter.clusters.Cluster>();

        HashMap<String, com.maximgalushka.classifier.twitter.clusters.Cluster> imagesIndex =
                new HashMap<String, com.maximgalushka.classifier.twitter.clusters.Cluster>();

        List<com.maximgalushka.classifier.twitter.clusters.Cluster> snapshot =
                Collections.unmodifiableList(clusters);

        for (com.maximgalushka.classifier.twitter.clusters.Cluster c : snapshot) {
            String message = c.getMessage();
            String url = c.getUrl();
            String image = c.getImage();
            if (messagesIndex.containsKey(message.trim())) mergeClusters(c, messagesIndex.get(message));
            else if (url != null && urlsIndex.containsKey(url)) mergeClusters(c, urlsIndex.get(url));
            else if (image != null && imagesIndex.containsKey(image)) mergeClusters(c, imagesIndex.get(image));
            else {
                messagesIndex.put(message.trim(), c);
                if (url != null) urlsIndex.put(url, c);
                if (image != null) imagesIndex.put(image, c);
            }
        }
        result.addAll(messagesIndex.values());
        return result;
    }

    private void mergeClusters(com.maximgalushka.classifier.twitter.clusters.Cluster from,
                               com.maximgalushka.classifier.twitter.clusters.Cluster to) {
        log.warn(String.format("Merging cluster [%d] to [%d]", from.getId(), to.getId()));
        // recalculate sore
        to.setScore(to.getScore() + from.getScore());
    }

    private static class TweetsComparator implements Comparator<Tweet> {
        @Override
        public int compare(Tweet one, Tweet two) {
            int oneScore = one.getFavouriteCount() + one.getRetweetCount();
            int twoScore = two.getFavouriteCount() + two.getRetweetCount();

            // higher - with higher mentions/retweets
            if (oneScore != twoScore) return twoScore - oneScore;

            // if scores are equal - just retrieve one which has media inside
            Entities e1 = one.getEntities();
            Entities e2 = two.getEntities();
            if (!e1.getMedia().isEmpty() && e2.getMedia().isEmpty()) return -1;
            if (e1.getMedia().isEmpty() && !e2.getMedia().isEmpty()) return 1;

            if (!e1.getUrls().isEmpty() && e2.getUrls().isEmpty()) return -1;
            if (e1.getUrls().isEmpty() && !e2.getUrls().isEmpty()) return 1;

            return 0;
        }
    }

    private static final Comparator<Tweet> TWEETS_COMPARATOR = new TweetsComparator();

    /**
     * Performance: O(n*log(n))
     *
     * @return finds good representative tweet from list of documents inside a single cluster
     */
    private Tweet findRepresentative(List<Document> allDocuments, Map<String, Tweet> tweetsIndex) {
        if (allDocuments.isEmpty()) return null;

        TreeSet<Tweet> selected = new TreeSet<Tweet>(TWEETS_COMPARATOR);
        for (Document d : allDocuments) {
            selected.add(tweetsIndex.get(d.getStringId()));
        }
        return selected.first();
    }

    /**
     * @return map which contain mapping between old cluster id which was migrated to new cluster id (optional if old cluster splitted)
     */
    private Map<Integer, Optional<Integer>> compareWithPrev(List<Cluster> prev, List<Cluster> current) {
        Map<Integer, Optional<Integer>> fromTo = new HashMap<Integer, Optional<Integer>>(prev.size() * 2);

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
        for (Cluster oldCluster : prev) {
            List<Document> docs = oldCluster.getAllDocuments();
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
                    //String message = currentCluster.getAllDocuments().get(0).getSummary();
                    //model.updateCluster(p.getId(), clusterId, currentCluster.getLabel(), message);
                    fromTo.put(oldCluster.getId(), Optional.fromNullable(clusterId));
                    log.debug(String.format("Cluster [%s] moved to [%s]", oldCluster.getId(), clusterId));
                    split = false;
                    break;
                }
                String currentLabel = currentCluster.getLabel();
                String prevLabel = oldCluster.getLabel();
                if (currentLabel.equals(prevLabel)) {
                    //String message = currentCluster.getAllDocuments().get(0).getSummary();
                    //model.updateCluster(oldCluster.getId(), clusterId, currentCluster.getLabel(), message);
                    fromTo.put(oldCluster.getId(), Optional.fromNullable(clusterId));
                    log.debug(String.format("Cluster [%s] moved to [%s]", oldCluster.getId(), clusterId));
                    split = false;
                    break;
                }
            }
            if (split) {
                //model.removeCluster(oldCluster.getId());
                fromTo.put(oldCluster.getId(), Optional.<Integer>absent());
                log.debug(String.format("Cluster [%s] splitted", oldCluster.getId()));
            }
        }
        return fromTo;
    }

    /**
     * Reads tweets to doument list ready for classification.<br/>
     * Filters out any duplicate tweets (with dame tweet id).
     */
    private List<Document> readTweetsToDocs(List<Tweet> tweets) throws IOException {
        List<Document> docs = new ArrayList<Document>(tweets.size());
        Set<String> set = new HashSet<String>(2 * docs.size());
        for (Tweet t : tweets) {
            String id = Long.toString(t.getId());
            if (!set.contains(id)) {
                docs.add(new Document(null, t.getText(), null, LanguageCode.ENGLISH, id));
                set.add(id);
            } else {
                log.debug(String.format("Skip duplicate document: [%s]", id));
            }
        }
        return docs;
    }

    private Map<String, Tweet> readTweetsToMap(List<Tweet> tweets) throws IOException {
        Map<String, Tweet> map = new HashMap<String, Tweet>(2 * tweets.size());
        for (Tweet t : tweets) {
            map.put(Long.toString(t.getId()), t);
        }
        return map;
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
                c.compareWithPrev(prev, clustersByTopic);
            }
            prev = clustersByTopic;

            if (stop) break;
            cleanFromStart(documents, delta);
        }
    }
}

