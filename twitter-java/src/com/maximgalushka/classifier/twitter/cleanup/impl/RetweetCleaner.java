package com.maximgalushka.classifier.twitter.cleanup.impl;

import com.maximgalushka.classifier.twitter.cleanup.Cleaner;

/**
 * @author Maxim Galushka
 */
public class RetweetCleaner implements Cleaner {

  @Override
  public String clean(String input) {
    return input.replaceAll("^\\s*(r|R)(t|T)\\s+@\\S+", "");
  }
}
