package com.maximgalushka.classifier.clustering.legacy.realtime;

import com.google.common.collect.Sets;
import com.maximgalushka.classifier.clustering.legacy.*;

import java.util.*;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public class ClusterClassifier {

    private StatsHolder stats;
    private TreeSet<Cluster> realtimeClusters = new TreeSet<Cluster>();

    public void classify(Document doc) {
        int t = 0;
        final long D = stats.addDocument(doc);
        final WordCountSparseVector global = stats.getGlobalVector();
        for (Cluster cluster : realtimeClusters) {
            TfIdfSparseVector clusterTfIdf = tfidf(global, D, cluster);
            TfIdfSparseVector documentTfIdf = tfidf(global, D, doc);
            double similarity = cosine(documentTfIdf, clusterTfIdf);
            if (similarity >= stats.getThreshold()) {
                // document is classified
                cluster.addDocument(doc);
                return;
            }
            if (t++ >= stats.getClustersToScan()) break;
        }
        // no suitable cluster found - create a new one just with this document
        // TODO: currently number of clusters can be only increased
        realtimeClusters.add(new Cluster(doc));
        stats.bumpClustersCount();
    }

    /**
     * @param a first vector
     * @param b second vector
     * @return cosine similarity between 2 documents
     */
    private double cosine(
            TfIdfSparseVector a,
            TfIdfSparseVector b) {
        Map<Integer, Double> aVector = a.getVector();
        Map<Integer, Double> bVector = b.getVector();
        Set<Integer> terms = Sets.union(aVector.keySet(), bVector.keySet());
        double totalSquare = 0D;
        double docSquare = 0D;
        double clusterSquare = 0D;
        for (int t : terms) {
            Double aValue = aVector.get(t);
            Double bValue = bVector.get(t);
            if (aValue != null && bValue != null) {
                totalSquare += Math.pow(aValue + bValue, 2);
                docSquare += Math.pow(aValue, 2);
                clusterSquare += Math.pow(bValue, 2);
            }
        }
        return Math.sqrt(totalSquare) /
                (Math.sqrt(docSquare) * Math.sqrt(clusterSquare));
    }

    /**
     * Calculates TF-IDF vector for current document.
     *
     * @param global   global word counts vector
     * @param D        total number of documents
     * @param document document to calculate TF-IDF for
     * @return TF-IDF vector for document
     */
    private TfIdfSparseVector tfidf(
            SparseVector<Long> global,
            Long D,
            Document document) {
        Map<Integer, Long> vector = document.getCenter().getVector();
        Map<Integer, Double> tfidf = new HashMap<Integer, Double>(vector.size());
        for (Integer hash : vector.keySet()) {
            Long tf = document.tf(hash);
            Double idf = idf(global, D, hash);
            tfidf.put(hash, tf * idf);
        }
        return new TfIdfSparseVector(tfidf);
    }


    /**
     * @param global global vector of counts of documents for each word
     * @param D      global count of documents
     * @param term   term to calculate IDF for
     * @return IDF(term)  = log (D / (1 + document_count(term)))
     */
    @SuppressWarnings("UnusedDeclaration")
    private double idf(SparseVector<Long> global, long D, String term) {
        return idf(global, D, term.hashCode());
    }

    /**
     * @param global global vector of counts of documents for each word
     * @param D      global count of documents
     * @param hash   hash code of the term to calculate IDF for
     * @return IDF(term)  = log (D / (1 + document_count(term)))
     */
    private double idf(SparseVector<Long> global, long D, Integer hash) {
        Long containingTerm = global.getVector().get(hash);
        if(containingTerm == null){
            containingTerm = 0L;
        }
        return Math.log((double) D / (1 + containingTerm));
    }

    public StatsHolder getStats() {
        return stats;
    }

    public void setStats(StatsHolder stats) {
        this.stats = stats;
    }
}
