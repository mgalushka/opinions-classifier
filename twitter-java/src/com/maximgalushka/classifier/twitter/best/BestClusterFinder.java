package com.maximgalushka.classifier.twitter.best;

import org.carrot2.core.Cluster;

import java.util.List;

/**
 * @author Maxim Galushka
 */
public class BestClusterFinder {

  /**
   * TODO: rethink.
   */
  @Deprecated
  public Cluster findRepresentativeCluster(List<Cluster> clusters) {
    if (clusters == null || clusters.isEmpty()) {
      return null;
    }
    return clusters.get(0);
  }
}
