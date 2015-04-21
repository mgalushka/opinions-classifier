package com.maximgalushka.classifier.twitter.best;

import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public final class FeaturesExtractorPipeline
  implements Feature<Map<String, Object>, Tweet> {

  public static final Logger log =
    Logger.getLogger(FeaturesExtractorPipeline.class);

  private List<Feature> featureExtractors = new ArrayList<>();

  public List<Feature> getFeatureExtractors() {
    return featureExtractors;
  }

  public void setFeatureExtractors(
    List<Feature> featureExtractors
  ) {
    this.featureExtractors = featureExtractors;
  }

  public void processBatch(List<Tweet> batch) {
    for (Tweet tweet : batch) {
      // this internally set excluded flag based on features
      extract(tweet);
    }
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public Map<String, Object> extract(Tweet input) {
    Map<String, Object> features = new HashMap<>();
    //String intermediate = input.getText();
    for (Feature extractor : featureExtractors) {
      Object feature = extractor.extract(input);
      String metricName = extractor.getClass().getSimpleName();
      features.put(
        metricName,
        extractor.metric(feature)
      );
      // TODO: not sure if this is good design but let set excluded flags
      // TODO: here as well
      if (extractor.exclude(feature)) {
        // TODO: change to trace
        log.debug(
          String.format(
            "Excluding tweet: [%s], because of feature [%s]",
            input,
            metricName
          )
        );
        input.setExcluded(true);
        input.setExcludedReason(
          String.format(
            "metric: [%s]%s",
            metricName,
            input.getExcludedReason()
          )
        );
      }
    }
    return features;
  }

  /**
   * @return sum of all the feature metrics for tweet from map
   */
  @Override
  public double metric(Map<String, Object> features) {
    Double sum = 0D;
    for (Map.Entry<String, Object> value : features.entrySet()) {
      sum += (Double) value.getValue();
    }
    return sum;
  }

  @Override
  public boolean exclude(Map<String, Object> features) {
    throw new RuntimeException(
      "Not supported exclude() method for features extractor"
    );
  }
}
