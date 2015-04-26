package com.maximgalushka.classifier.clustering.lsh.simhash;

import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class SimhashClusteringTest {

  public static final Logger log = Logger.getLogger(SimhashClusteringTest.class);

  private static final List<Tweet> tweets =
    Arrays.asList(
      new Tweet(
        "Co-operation is required when the deal is almost ready to signed-off by both competitive" +
          " sides"
      ),
      new Tweet(
        "The weather in Atlanta this week is not same as in Georgia but gets worse."
      ),
      new Tweet(
        "Co-operation is required when the deal is almost ready to signed-off by both competitive" +
          " sides"
      ),
      new Tweet(
        "Co-operation is required when the deal is almost ready to signed-off by both competitive" +
          " sides"
      ),
      new Tweet(
        "Communication is required when deal is almost to be signed-off by both " +
          "competitive" +
          " sides"
      ),
      new Tweet(
        "The weather in Atlanta (?) this week is not same as in Georgia but get worse."
      )
    );

  @Test
  public void testGetClusters() throws Exception {
    SimhashClustering clustering = new SimhashClustering(12);
    List<List<Tweet>> clusters = clustering.getClusters(tweets);

    Assert.assertEquals(2, clusters.size());
    Assert.assertEquals(4, clusters.get(0).size());
    Assert.assertEquals(2, clusters.get(1).size());

    int count = 0;
    for (List<Tweet> cluster : clusters) {
      log.debug(
        String.format(
          "Documents in cluster(%d) = %d: %s",
          count++,
          cluster.size(),
          cluster
        )
      );
    }
  }
}