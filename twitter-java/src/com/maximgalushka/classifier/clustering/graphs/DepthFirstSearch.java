package com.maximgalushka.classifier.clustering.graphs;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Note - this class has internal state and once traversed via DFS from some node -
 * it is impossible to traverse same connected component even from other node.
 * Visited nodes are memoized in internal state of object.
 *
 * NOTE! We are executing visitor code on all vertices except first node passed to DFS
 * as this is what expected byspecification.
 *
 * So this is limited implementation of DFS and cannot be reusable for other use-cases.
 *
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
@NotThreadSafe
public class DepthFirstSearch {
  private final Graph G;
  private final DfsAction action;
  private final boolean[] marked;    // marked[v] = is node v already visited?
  private int count;                 // number of vertices processed by DFS

  public interface DfsAction {
    void process(int vertexId);
  }

  /**
   * Computes the vertices in graph <tt>G</tt> that are
   * connected to the source vertex <tt>s</tt>.
   *
   * @param G the graph
   */
  public DepthFirstSearch(Graph G, DfsAction action) {
    this.marked = new boolean[G.V()];
    this.G = G;
    this.action = action;
  }

  // depth first search from v
  // note that we are executing visitor code on all vertices except v by itself
  public void dfs(int v) {
    if (marked(v)) {
      return;
    }
    count++;
    marked[v] = true;
    for (int w : G.adj(v)) {
      if (!marked[w]) {
        action.process(w);
        dfs(w);
      }
    }
  }

  /**
   * Is there a path between the source vertex <tt>s</tt> and vertex <tt>v</tt>?
   *
   * @param v the vertex
   * @return <tt>true</tt> if there is a path, <tt>false</tt> otherwise
   */
  public boolean marked(int v) {
    return marked[v];
  }

  /**
   * Returns the number of vertices connected to the source vertex <tt>s</tt>.
   *
   * @return the number of vertices connected to the source vertex <tt>s</tt>
   */
  public int count() {
    return count;
  }

}