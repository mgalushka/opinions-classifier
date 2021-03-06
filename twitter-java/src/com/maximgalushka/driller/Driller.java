package com.maximgalushka.driller;

import java.io.IOException;

/**
 * Driller is a service which resolves short urls to actual final ones.
 * It can resolve redirects primarily.
 *
 * @author Maxim Galushka
 */
public interface Driller {

  /**
   * Resolves url by following redirects.
   *
   * @param url input URL
   * @return resolved final URL of resource
   */
  String resolve(String url) throws IOException;

  /**
   * Resolves url asynchronously.
   */
  void asyncResolve(String url, DrillerCallback callback);
}
