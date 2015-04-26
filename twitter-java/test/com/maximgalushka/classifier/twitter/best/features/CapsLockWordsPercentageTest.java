package com.maximgalushka.classifier.twitter.best.features;

import com.maximgalushka.classifier.twitter.model.Tweet;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class CapsLockWordsPercentageTest {

  private static final String T = "SOME CAP-LOCK tweet with some of the words.";
  private static final String T1 = "SOME CAP-LOCK TWEET WITH words. Etc.";

  @Test
  public void testExtract() throws Exception {
    CapsLockWordsPercentage metric = new CapsLockWordsPercentage();
    Double m = metric.extract(new Tweet(T));
    Assert.assertEquals(2d / 8d, m, 1E-5);

    m = metric.extract(new Tweet(T1));
    Assert.assertEquals(4d / 6d, m, 1E-5);

  }
}