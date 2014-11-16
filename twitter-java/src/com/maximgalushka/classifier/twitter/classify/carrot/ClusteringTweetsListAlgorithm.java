package com.maximgalushka.classifier.twitter.classify.carrot;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.clusters.*;
import com.maximgalushka.classifier.twitter.model.Entities;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.maximgalushka.classifier.twitter.model.TweetTextWrapper;
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
public class ClusteringTweetsListAlgorithm {

    public static final Logger log = Logger.getLogger(
            ClusteringTweetsListAlgorithm.class);

    private List<Cluster> previousClusters;
    private static final ClusteringTweetsListAlgorithm algorithm = new ClusteringTweetsListAlgorithm();

    private AtomicInteger ai = new AtomicInteger(0);
    private Controller controller;
    private StorageService storage;

    private ClusteringTweetsListAlgorithm() {
        // A controller to manage the processing pipeline.
        //controller = ControllerFactory.createSimple();

        // generic storage service
        //storage = StorageService.getService();
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setStorage(StorageService storage) {
        this.storage = storage;
    }

    public static ClusteringTweetsListAlgorithm getAlgorithm() {
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

    /**
     * @param clusters in-memory current clusters snapshot model - this is singleton object stored on app level
     */
    private void updateModel(@Deprecated final Clusters clusters, List<Cluster> currentClusters,
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
        synchronized (this) {
            clusters.cleanClusters();
            List<com.maximgalushka.classifier.twitter.clusters.Cluster> finalList = filterAndFormatRepresetnations(updated);
            log.debug(String.format("Final clusters list: [%s]", finalList));
            clusters.addClusters(finalList);

            // storing
            storage.saveNewClustersGroup(clusters);
        }
    }

    /**
     * TODO: kind of messy method to filter out duplicate cluster representations if any.
     *
     * @return list of clusters (domain model) without duplicated
     */
    private List<com.maximgalushka.classifier.twitter.clusters.Cluster>
    filterAndFormatRepresetnations(List<com.maximgalushka.classifier.twitter.clusters.Cluster> clusters) {
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

        for (com.maximgalushka.classifier.twitter.clusters.Cluster cluster : result) {
            cluster.setMessage(reformatMessage(cluster.getMessage()));
        }

        return result;
    }

    /**
     * <ul>
     * <li>Removes all "RT" asking for retweet.</li>
     * <li>Removes all the mentions.</li>
     * <li>Clean-up all the urls</li>
     * </ul>
     */
    public String reformatMessage(String initial) {
        String formatted = initial.replaceAll("(r|R)(t|T)", "");
        formatted = formatted.replaceAll("@\\S+", "");
        formatted = formatted.replaceAll("http[s]?:[/]{1,2}\\S*", "");

        // remove all URLs' remains
        formatted = formatted.replaceAll("https:", "");
        formatted = formatted.replaceAll("http:", "");
        formatted = formatted.replaceAll("https", "");
        formatted = formatted.replaceAll("http", "");

        // normalize internal spaces
        formatted = formatted.replaceAll("\\s+", " ");
        return formatted.trim();
    }

    private void mergeClusters(com.maximgalushka.classifier.twitter.clusters.Cluster from,
                               com.maximgalushka.classifier.twitter.clusters.Cluster to) {
        log.warn(String.format("Merging cluster [%d] to [%d]", from.getId(), to.getId()));
        // recalculate sore
        to.setScore(to.getScore() + from.getScore());
    }

    /**
     * Performance: O(n*log(n))<br/>
     * O(n^2)!!!<br/>
     *
     * @return finds good representative tweet from list of documents inside a single cluster
     */
    private Tweet findRepresentative(List<Document> allDocuments, Map<String, Tweet> tweetsIndex) {
        if (allDocuments.isEmpty()) return null;

        /*
        TreeSet<Tweet> selected = new TreeSet<Tweet>(TWEETS_COMPARATOR);
        for (Document d : allDocuments) {
            selected.add(tweetsIndex.get(d.getStringId()));
        }
        return selected.first();
        */

        double T = 0.5D;
        // combined tweet representative -> count of such tweets (similar based on Jaccard coefficient)
        HashMap<TweetTextWrapper, Integer> similarity = new HashMap<TweetTextWrapper, Integer>();
        for (Document d : allDocuments) {
            Tweet found = tweetsIndex.get(d.getStringId());
            String foundText = found.getText();
            boolean similar = false;
            for (TweetTextWrapper w : similarity.keySet()) {
                if (jaccard(foundText, w.getText()) >= T) {
                    similarity.put(w, similarity.get(w) + 1);
                    Tweet underlying = w.getTweet();

                    // fill missing media - to enrich representative
                    if (underlying.getEntities().getMedia().isEmpty() && !found.getEntities().getMedia().isEmpty()) {
                        underlying.getEntities().getMedia().addAll(found.getEntities().getMedia());
                    }
                    if (underlying.getEntities().getUrls().isEmpty() && !found.getEntities().getUrls().isEmpty()) {
                        underlying.getEntities().getUrls().addAll(found.getEntities().getUrls());
                    }

                    similar = true;
                    break;
                }
            }
            if (!similar) {
                similarity.put(new TweetTextWrapper(foundText, found), 1);
            }
        }
        // find top tweet with max number of similar to it
        int max = 0;
        Tweet representative = null;
        for (TweetTextWrapper w : similarity.keySet()) {
            int current = similarity.get(w);
            if (current > max) {
                max = current;
                representative = w.getTweet();
            }
        }
        return representative;
    }

    public double jaccard(String a, String b) {
        Set<String> tokens_A = Sets.newHashSet(a.split("\\s+"));
        Set<String> tokens_B = Sets.newHashSet(b.split("\\s+"));

        int intersection = Sets.intersection(tokens_A, tokens_B).size();
        int union = Sets.union(tokens_A, tokens_B).size();

        return (double) intersection / union;
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
     * Reads tweets to document list ready for classification.<br/>
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
        ClusteringTweetsListAlgorithm c = new ClusteringTweetsListAlgorithm();

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

    /*
    TODO: old code - consider removing
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
    */
}

