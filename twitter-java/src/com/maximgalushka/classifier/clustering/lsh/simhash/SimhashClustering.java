package com.maximgalushka.classifier.clustering.lsh.simhash;

import com.maximgalushka.classifier.clustering.graphs.ConnectedComponents;
import com.maximgalushka.classifier.clustering.graphs.Graph;
import com.maximgalushka.classifier.twitter.model.Tweet;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SimhashClustering {

  private final SimhashDuplicates duplicates;

  public SimhashClustering(int levenstainThreashold) {
    this.duplicates = new SimhashDuplicates(levenstainThreashold);
  }

  /**
   * Performs simhash clustering
   *
   * @param tweets tweets to cluster
   * @return list of clusters
   */
  public List<List<Tweet>> getClusters(List<Tweet> tweets) {
    Graph graph = duplicates.buildTweetsGraph(tweets);

    ConnectedComponents cc = new ConnectedComponents(graph);
    List<List<Tweet>> clusters = new ArrayList<>(cc.count());
    for (int index = 0; index < cc.count(); index++) {
      clusters.add(new ArrayList<>());
    }

    for (int index = 0; index < tweets.size(); index++) {
      Tweet current = tweets.get(index);
      int clusterId = cc.id(index);
      clusters.get(clusterId).add(current);
    }

    return clusters;
  }
}
