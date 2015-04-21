package com.maximgalushka.classifier.twitter.cleanup;

import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.model.Tweet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Maxim Galushka
 */
public class BlacklistProcessor {

  private LocalSettings settings;

  public void setSettings(LocalSettings settings) {
    this.settings = settings;
  }

  public List<Tweet> clean(List<Tweet> tweets) {
    List<Tweet> cleaned = new ArrayList<>();
    String black = settings.value(LocalSettings.TWITTER_BLACKLIST);
    List<String> banned = Arrays.asList(black.toLowerCase().split(","));
    for (Tweet tweet : tweets) {
      boolean excluded = false;
      String forbiddenToken = "";
      for (String b : banned) {
        if (tweet.getText().toLowerCase().contains(b)) {
          excluded = true;
          forbiddenToken = b;
          break;
        }
      }
      if (excluded) {
        tweet.setExcluded(true);
        tweet.setExcludedReason(
          String.format(
            "blacklisted words: [%s]%s",
            forbiddenToken,
            tweet.getExcludedReason()
          )
        );
      } else {
        cleaned.add(tweet);
      }
    }
    return cleaned;
  }
}
