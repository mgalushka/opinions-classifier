package com.maximgalushka.classifier.twitter.best;

import com.maximgalushka.classifier.twitter.model.Tweet;

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
      features.put(
        extractor.getClass().toString(),
        extractor.extract(intermediate)
      );
    }
    return features;
  }
}
