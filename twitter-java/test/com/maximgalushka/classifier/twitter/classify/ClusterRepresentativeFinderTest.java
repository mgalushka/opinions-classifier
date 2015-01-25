package com.maximgalushka.classifier.twitter.classify;


import com.maximgalushka.classifier.twitter.model.Tweet;
import org.junit.Test;
import org.testng.Assert;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maxim Galushka
 */
public class ClusterRepresentativeFinderTest {

  @Test
  public void testFindRepresentativeScoreBased()
  throws ParserConfigurationException, SAXException, IOException {
    ClusterRepresentativeFinder finder = new ClusterRepresentativeFinder();
    Map<String, Tweet> inversed = new HashMap<>();

    Tweet a = new Tweet("This isl correct sentence");
    Tweet b = new Tweet("This is correct sentence");
    Tweet c = new Tweet("This is atn corrects #sentence");
    inversed.put("1", a);
    inversed.put("2", b);
    inversed.put("3", c);

    Tweet found = finder.findRepresentativeScoreBased(inversed);
    Assert.assertEquals(found.getText(), "This is correct sentence");
  }

  @Test
  public void testGetTweetScore()
  throws ParserConfigurationException, SAXException, IOException {
    ClusterRepresentativeFinder finder = new ClusterRepresentativeFinder();
    double d = finder.getTweetScore(new Tweet("This is correct sentence"));
    Assert.assertEquals(d, -96D);

    d = finder.getTweetScore(new Tweet("This isl correct sentence"));
    Assert.assertEquals(d, -95D);

    d = finder.getTweetScore(new Tweet("This is atn corrects #sentence"));
    Assert.assertEquals(d, -95D);
  }
}
