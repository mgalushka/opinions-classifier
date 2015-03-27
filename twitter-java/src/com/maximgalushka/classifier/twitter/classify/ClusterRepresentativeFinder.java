package com.maximgalushka.classifier.twitter.classify;

import com.maximgalushka.classifier.twitter.model.Tweet;
import com.maximgalushka.classifier.twitter.model.TweetTextWrapper;
import org.carrot2.core.Document;
import org.languagetool.JLanguageTool;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

/**
 * @author Maxim Galushka
 */
public class ClusterRepresentativeFinder {

  private JLanguageTool languageTool;

  public ClusterRepresentativeFinder()
  throws IOException, ParserConfigurationException, SAXException {
    languageTool = new MultiThreadedJLanguageTool(
      new BritishEnglish(),
      null
    );
    languageTool.activateDefaultPatternRules();
    languageTool.activateDefaultFalseFriendRules();
  }

  /**
   * Performance: O(n*log(n))<br/>
   * O(n^2)!!!<br/>
   * <p>
   * Does not use any language tools
   *
   * @return finds good representative tweet from list of documents inside a
   * single cluster
   */
  @SuppressWarnings("UnusedDeclaration")
  @Deprecated
  public Tweet findRepresentativeLegacy(
    List<Document> documents,
    Map<String, Tweet> tweetsIndex
  ) {
    if (tweetsIndex.isEmpty()) {
      return null;
    }

    double T = 0.5D;
    // combined tweet representative -> count of such tweets (similar based
    // on Jaccard coefficient)
    HashMap<TweetTextWrapper, Integer> similarity = new HashMap<>();
    for (Document document : documents) {
      Tweet found = tweetsIndex.get(document.getStringId());
      String foundText = found.getText();
      boolean similar = false;
      for (TweetTextWrapper w : similarity.keySet()) {
        if (Tools.jaccard(foundText, w.getText()) >= T) {
          similarity.put(w, similarity.get(w) + 1);
          Tweet underlying = w.getTweet();

          // fill missing media - to enrich representative
          if (underlying.getEntities().getMedia().isEmpty() &&
            !found.getEntities().getMedia().isEmpty()) {
            underlying.getEntities()
                      .getMedia()
                      .addAll(found.getEntities().getMedia());
          }
          if (underlying.getEntities().getUrls().isEmpty() &&
            !found.getEntities().getUrls().isEmpty()) {
            underlying.getEntities()
                      .getUrls()
                      .addAll(found.getEntities().getUrls());
          }

          similar = true;
          break;
        }
      }
      if (!similar) {
        similarity.put(new TweetTextWrapper(foundText, found), 1);
      }
    }
    // find top tweet with max number of similar to it
    int max = 0;
    Tweet representative = null;
    for (TweetTextWrapper w : similarity.keySet()) {
      int current = similarity.get(w);
      if (current > max) {
        max = current;
        representative = w.getTweet();
      }
    }
    return representative;
  }

  public Tweet findRepresentativeScoreBased(
    List<Tweet> cluster
  ) {
    if (cluster.isEmpty()) {
      return null;
    }

    TreeMap<TweetTextWrapper, Integer> sorted = new TreeMap<>(
      new TweetTextWrapperComparable()
    );
    for (Tweet tweet : cluster) {
      int score = getTweetScore(tweet);
      sorted.put(new TweetTextWrapper(tweet.getText(), tweet), score);
    }
    logRepresentative(sorted);
    return sorted.firstEntry().getKey().getTweet();
  }

  /**
   * Finds representative from cluster using next technique:
   * Calculates score for each document.
   * Picks up document with highest score.
   * <p>
   * Then we will pick top 25% best tweets and find the element which
   * is mostly looks like others (jaccard similarity).
   * <p>
   * This will avoid bad element to be chosen as representative.
   * <p>
   * O(n*log(n))
   *
   * @param tweetsIndex tweets reversed index - to speed up search
   * @return best tween in cluster
   */
  public Tweet findRepresentativeScoreBased(
    List<Document> documents,
    Map<String, Tweet> tweetsIndex
  ) {
    if (tweetsIndex.isEmpty()) {
      return null;
    }

    TreeMap<TweetTextWrapper, Integer> sorted = new TreeMap<>(
      new TweetTextWrapperComparable()
    );
    for (Document document : documents) {
      Tweet t = tweetsIndex.get(document.getStringId());
      int score = getTweetScore(t);
      sorted.put(new TweetTextWrapper(t.getText(), t), score);
    }
    logRepresentative(sorted);
    return sorted.firstEntry().getKey().getTweet();
  }

  private void logRepresentative(
    final TreeMap<TweetTextWrapper, Integer>
      sorted
  ) {
    StringBuilder sb = new StringBuilder("\n");
    for (TweetTextWrapper tw : sorted.keySet()) {
      sb.append(
        String.format(
          "tweet: [%s]",
          tw.getText().replaceAll("\\s+", " ")
        )
      )
        .append("\t")
        .append(String.format("score: [%d]", sorted.get(tw)))
        .append("\n");
    }
    Tweet chosen = sorted.firstEntry().getKey().getTweet();
    sb.append(
      String.format(
        "Chosen tweet: [%s]", chosen.getText().replaceAll(
          "\\s+", " "
        )
      )
    )
      .append("\t")
      .append(
        String.format(
          "score: [%d]", sorted.get(
            sorted.firstEntry().getKey()
          )
        )
      )
      .append("\n");

    System.out.println(sb.toString());
  }

  private class TweetTextWrapperComparable
    implements Comparator<TweetTextWrapper> {
    @Override
    public int compare(
      TweetTextWrapper first, TweetTextWrapper second
    ) {
      return
        ClusterRepresentativeFinder.this.getTweetScore(first.getTweet()) -
          ClusterRepresentativeFinder.this.getTweetScore(second.getTweet());
    }
  }

  public int getTweetScore(Tweet t) {
    String text = t.getText();
    int errors = 0;
    try {
      errors = languageTool.check(text).size();
    } catch (IOException e) {
      e.printStackTrace();
    }

    int max = 100;
    int urlScore = (t.getEntities() == null ||
      t.getEntities().getUrls() == null ||
      t.getEntities().getUrls().isEmpty()) ? -3 : 0;
    int imageScore = (t.getEntities() == null ||
      t.getEntities().getMedia() == null ||
      t.getEntities().getMedia().isEmpty()) ? -1 : 0;

    // higher this score - higher probability that item will be picked
    int finalScore = max - errors + urlScore + imageScore;

    // because TreeMap
    return -finalScore;
  }

}
