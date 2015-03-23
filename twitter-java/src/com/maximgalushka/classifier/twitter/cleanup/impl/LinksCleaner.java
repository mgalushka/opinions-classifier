package com.maximgalushka.classifier.twitter.cleanup.impl;

import com.maximgalushka.classifier.twitter.cleanup.Cleaner;

/**
 * @author Maxim Galushka
 */
public class LinksCleaner implements Cleaner {

  @Override
  public String clean(String input) {
    return cleanFullUrls(cleanEndUrls(input));
  }

  public String cleanFullUrls(String input) {
    return input.replaceAll("http[s]?:[/]{1,2}\\S*", "");
  }

  public String cleanEndUrls(String input) {
    // remove all URLs' remains at the end of the string
    return input.replaceAll(
      "(http[s]?:[/]{1,2}\\S*|http[s]?:|http[s]?|htt|ht|h)(\\.*|," +
        "|-|!|\\?)?\\s*$",
      ""
    );
  }
}
