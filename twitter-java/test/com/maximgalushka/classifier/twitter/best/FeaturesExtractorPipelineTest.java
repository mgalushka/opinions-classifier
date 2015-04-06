package com.maximgalushka.classifier.twitter.best;

import com.maximgalushka.classifier.twitter.best.features.HashtagsCount;
import com.maximgalushka.classifier.twitter.best.features.MentionsCount;
import com.maximgalushka.classifier.twitter.best.features.SpellingMistakesCount;
import com.maximgalushka.classifier.twitter.best.features.WordCount;
import org.junit.Test;
import org.testng.Assert;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class FeaturesExtractorPipelineTest {

  private static final String T = "'RT @GrahamWP_UK: \"Ukraine’s neo-Nazi leader (Dmitry " +
    "@Yarosh) becomes top military #adviser, legalizes fighters\" http://t" +
    ".co/VkX6Rz4J7Z #Donbass";

  @Test
  public void testWordCount(){
    WordCount wc = new WordCount();
    Assert.assertEquals(15L, (long) wc.extract(T));
  }

  @Test
   public void testHashtagsCount(){
    HashtagsCount htc = new HashtagsCount();
    Assert.assertEquals(2L, (long) htc.extract(T));
  }

  @Test
  public void testMentionsCount(){
    MentionsCount mc = new MentionsCount();
    Assert.assertEquals(2L, (long) mc.extract(T));
  }

  @Test
  public void testSpellingMistakesCount()
  throws ParserConfigurationException, SAXException, IOException {
    SpellingMistakesCount smc = new SpellingMistakesCount();
    Assert.assertEquals(8L, (long) smc.extract(T));
  }

}