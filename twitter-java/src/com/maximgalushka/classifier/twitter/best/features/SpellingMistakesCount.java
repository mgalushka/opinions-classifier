package com.maximgalushka.classifier.twitter.best.features;

import com.maximgalushka.classifier.twitter.best.TextCounterFeature;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.languagetool.JLanguageTool;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 *
 */
public final class SpellingMistakesCount implements TextCounterFeature {

  private final JLanguageTool languageTool;

  public SpellingMistakesCount()
    throws
    IOException,
    ParserConfigurationException,
    SAXException {

    languageTool = new MultiThreadedJLanguageTool(
      new BritishEnglish(),
      null
    );
    languageTool.activateDefaultPatternRules();
    languageTool.activateDefaultFalseFriendRules();
  }

  @Override
  public Long extract(Tweet tweet) {
    int errors = 0;
    // we need to ignore hashtags when checking for spelling
    String withoutHashTags = tweet.getText().replace("#", "");
    try {
      errors = languageTool.check(withoutHashTags).size();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return (long) errors;
  }

  @Override
  public double metric(Long feature) {
    return feature / 2D;
  }

  @Override
  public boolean exclude(Long feature) {
    // we don't excluded on mistakes count now
    return false;
  }
}
