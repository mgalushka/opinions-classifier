package com.maximgalushka.classifier.twitter.best;

import com.maximgalushka.classifier.twitter.model.Tweet;

import java.util.*;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public final class FeaturesExtractorPipeline
  implements Feature<Map<String, Object>, Tweet> {

  private List<Feature> featureExtractors = new ArrayList<>();

  public List<Feature> getFeatureExtractors() {
    return featureExtractors;
  }

  public void setFeatureExtractors(
    List<Feature> featureExtractors
  ) {
    this.featureExtractors = featureExtractors;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public Map<String, Object> extract(Tweet input) {
    Map<String, Object> features = new HashMap<>();
    String intermediate = input.getText();
    for (Feature extractor : featureExtractors) {
      Object feature = extractor.extract(intermediate);
      features.put(
        extractor.getClass().getSimpleName(),
        extractor.metric(feature)
      );
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
}
