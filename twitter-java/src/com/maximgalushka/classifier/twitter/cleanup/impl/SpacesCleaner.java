package com.maximgalushka.classifier.twitter.cleanup.impl;

import com.maximgalushka.classifier.twitter.cleanup.Cleaner;

/**
 * @author Maxim Galushka
 */
public class SpacesCleaner implements Cleaner {

  @Override
  public String clean(String input) {
    // normalize internal spaces
    return input.replaceAll("\\s+", " ").trim();
  }
}
