package com.maximgalushka.classifier.clustering.graphs;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public class DepthFirstSearch {
  private boolean[] marked;    // marked[v] = is there an s-v path?
  private int count;           // number of vertices connected to s

  public static interface DfsAction {
     void process(int vertexId);
  }

  /**
   * Computes the vertices in graph <tt>G</tt> that are
   * connected to the source vertex <tt>s</tt>.
   *
   * @param G the graph
   * @param s the source vertex
   */
  public DepthFirstSearch(Graph G, int s, DfsAction action) {
    marked = new boolean[G.V()];
    dfs(G, s, action);
  }

  // depth first search from v
  private void dfs(Graph G, int v, DfsAction action) {
    count++;
    marked[v] = true;
    for (int w : G.adj(v)) {
      if (!marked[w]) {
        action.process(w);
        dfs(G, w, action);
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