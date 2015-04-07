package com.maximgalushka.classifier.twitter.best;

import com.maximgalushka.classifier.twitter.classify.Tools;
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
@SuppressWarnings({"UnusedDeclaration", "deprecation"})
public class ClusterRepresentativeFinder {

  private JLanguageTool languageTool;
  private FeaturesExtractorPipeline featuresExtractor;

  public FeaturesExtractorPipeline getFeaturesExtractor() {
    return featuresExtractor;
  }

  public void setFeaturesExtractor(
    FeaturesExtractorPipeline
      featuresExtractor
  ) {
    this.featuresExtractor = featuresExtractor;
  }

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

  public static <K, V extends Comparable<V>> Map<K, V>
  sortByValues(final Map<K, V> map) {
    Comparator<K> valueComparator =
      (k1, k2) -> {
        int compare =
          map.get(k1).compareTo(map.get(k2));
        if (compare == 0) {
          return 1;
        } else {
          return compare;
        }
      };

    Map<K, V> sortedByValues =
      new TreeMap<>(valueComparator);
    sortedByValues.putAll(map);
    return sortedByValues;
  }

  public static class Pair<A, B> {
    public A a;
    public B b;

    public Pair(A a, B b) {
      this.a = a;
      this.b = b;
    }

    public A getA() {
      return a;
    }

    public B getB() {
      return b;
    }
  }

  private static final String METRIC = "METRIC";

  /**
   * @return pair -
   * best tweet and full map of features for each tweet in current cluster.
   */
  public Pair<Tweet, Map<Tweet, Map<String, Object>>>
  findRepresentativeFeaturesBased(
    List<Tweet> cluster
  ) {
    if (cluster.isEmpty()) {
      return null;
    }

    Map<Tweet, Map<String, Object>> features = new HashMap<>();
    Map<Tweet, Double> tweets = new HashMap<>();
    for (Tweet tweet : cluster) {
      Map<String, Object> ftrs = featuresExtractor.extract(tweet);
      double score = featuresExtractor.metric(ftrs);
      tweets.put(tweet, score);

      // adding total sum metric to list of features to save in DB.
      ftrs.put(METRIC, score);
      features.put(tweet, ftrs);
    }
    return new Pair<>(
      sortByValues(tweets).keySet().iterator().next(),
      features
    );
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
  @Deprecated
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

  @Deprecated
  private void logRepresentative(
    final TreeMap<TweetTextWrapper, ? extends Number>
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
        .append(String.format("score: [%f]", sorted.get(tw).doubleValue()))
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
          "score: [%f]", sorted.get(
            sorted.firstEntry().getKey()
          ).doubleValue()
        )
      )
      .append("\n");

    System.out.println(sb.toString());
  }

  @Deprecated
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

  /**
   * @deprecated use features pipeline and extractors
   */
  @Deprecated
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
