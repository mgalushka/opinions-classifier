package com.maximgalushka.classifier.twitter.cleanup;

import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.commons.lang.StringUtils;

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
    String from = settings.value(LocalSettings.TWITTER_FROM_BLACKLIST);
    List<String> banned = Arrays.asList(black.toLowerCase().split(","));
    List<String> bannedUsers = Arrays.asList(from.toLowerCase().split(","));
    for (Tweet tweet : tweets) {
      boolean excluded = false;
      String forbiddenToken = "";
      String forbiddenUser = "";
      for (String user : bannedUsers) {
        if (tweet.getAuthor().getName().equals(user)) {
          excluded = true;
          forbiddenUser = tweet.getAuthor().getName();
          break;
        }
      }
      if (!excluded) {
        for (String b : banned) {
          if (tweet.getText().toLowerCase().contains(b)) {
            excluded = true;
            forbiddenToken = b;
            break;
          }
        }
      }
      if (excluded) {
        tweet.setExcluded(true);
        tweet.setExcludedReason(
          StringUtils.isNotBlank(forbiddenToken) ?
            String.format(
              "blacklisted words: [%s]%s",
              forbiddenToken,
              tweet.getExcludedReason()
            ) :
            (StringUtils.isNotBlank(forbiddenUser) ?
              String.format(
                "blacklisted user: [%s]%s",
                forbiddenUser,
                tweet.getExcludedReason()
              ) : "")
        );
      } else {
        cleaned.add(tweet);
      }
    }
    return cleaned;
  }
}
