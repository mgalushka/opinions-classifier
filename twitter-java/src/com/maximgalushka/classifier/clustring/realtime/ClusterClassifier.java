package com.maximgalushka.classifier.clustring.realtime;

import com.google.common.collect.Sets;
import com.maximgalushka.classifier.clustring.model.Cluster;
import com.maximgalushka.classifier.clustring.model.Document;
import com.maximgalushka.classifier.clustring.model.IdfSparseVector;

import java.util.*;

/**
 * @author Maxim Galushka
 */
public class ClusterClassifier {

    private StatsHolder stats;
    private TreeSet<Cluster> realtimeClusters = new TreeSet<Cluster>();

    public void classify(Document doc) {
        int t = 0;
        long D = stats.addDocument(doc);
        for (Cluster cluster : realtimeClusters) {
            double similarity = cosine(
                    stats.getGlobalVector(),
                    stats.getDocumentsCount(),
                    doc.getCenter(),
                    cluster.getCenter());
            if (similarity >= stats.getThreshold()) {
                cluster.addDocument(doc);
            }
            if (t++ <= stats.getClustersToScan()) break;
        }
        realtimeClusters.add(new Cluster(doc));
    }

    private double cosine(IdfSparseVector global, long D, IdfSparseVector doc,
                          IdfSparseVector cluster) {
        Map<Integer, Long> docTerms = doc.getVector();
        Map<Integer, Long> clusterTerms = cluster.getVector();
        Set<Integer> terms = Sets.union(docTerms.keySet(), clusterTerms.keySet());
        double totalSquare = 0D;
        double docSquare = 0D;
        double clusterSquare = 0D;
        for (int t : terms) {
            Long docCount = docTerms.get(t);
            Long clusterCount = clusterTerms.get(t);
            if (docCount != null && clusterCount != null) {
                totalSquare += Math.pow(docCount + clusterCount, 2);
                docSquare += Math.pow(docCount, 2);
                clusterSquare += Math.pow(clusterCount, 2);
            }
        }
        return Math.sqrt(totalSquare) / (Math.sqrt(docSquare) * Math.sqrt(clusterSquare));
    }

}
