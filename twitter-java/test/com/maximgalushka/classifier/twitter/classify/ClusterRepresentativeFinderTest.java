package com.maximgalushka.classifier.twitter.classify;

import com.maximgalushka.classifier.twitter.best.ClusterRepresentativeFinder;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Galushka
 */
public class ClusterRepresentativeFinderTest {

  @Test
  public void testFindRepresentativeScoreBased()
  throws ParserConfigurationException, SAXException, IOException {
    ClusterRepresentativeFinder finder = new ClusterRepresentativeFinder();
    List<Tweet> inverse = new ArrayList<>();

    inverse.add(new Tweet("This isl correct sentence"));
    inverse.add(new Tweet("This is correct sentence"));
    inverse.add(new Tweet("This is atn corrects #sentence"));

    Tweet found = finder.findRepresentativeScoreBased(inverse);
    Assert.assertEquals(found.getText(), "This is correct sentence");
  }

  @Test
  public void testGetTweetScore()
  throws ParserConfigurationException, SAXException, IOException {
    ClusterRepresentativeFinder finder = new ClusterRepresentativeFinder();
    double d = finder.getTweetScore(new Tweet("This is correct sentence"));
    Assert.assertEquals(d, -96D, 1e-10D);

    d = finder.getTweetScore(new Tweet("This isl correct sentence"));
    Assert.assertEquals(d, -95D, 1e-10D);

    d = finder.getTweetScore(new Tweet("This is atn corrects #sentence"));
    Assert.assertEquals(d, -95D, 1e-10D);
  }
}
