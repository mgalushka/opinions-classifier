package com.maximgalushka.classifier.clustering.graphs;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class DepthFirstSearchTest {

  public static final Logger log = Logger.getLogger(DepthFirstSearchTest.class);

  @Test
  public void testDfs() throws Exception {
    int SIZE = 5;
    Graph g = new Graph(SIZE);
    g.addEdge(0, 1);
    g.addEdge(2, 3);
    g.addEdge(1, 4);

    int[] connected = new int[SIZE];
    DepthFirstSearch dfs = new DepthFirstSearch(
      g,
      vertexId -> {
        connected[vertexId]++;
        log.debug(
          String.format(
            "Visiting [%d]",
            vertexId
          )
        );
      }
    );
    dfs.dfs(0);
    int[] expected_1 = {0, 1, 0, 0, 1};
    Assert.assertArrayEquals(expected_1, connected);

    dfs.dfs(2);
    int[] expected_2 = {0, 1, 0, 1, 1};
    // now we shoukld be visited 3 vertex as well after DFS from 2
    Assert.assertArrayEquals(expected_2, connected);

    // try DFS from already visited component of graph
    dfs.dfs(4);
    dfs.dfs(1);
    dfs.dfs(2);
    dfs.dfs(3);
    //int[] expected_3 = {0, 1, 0, 1, 1};
    // now visited list should be the same as it was memoized inside
    Assert.assertArrayEquals(expected_2, connected);
  }
}