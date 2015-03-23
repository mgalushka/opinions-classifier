package com.maximgalushka.classifier.twitter.cleanup.impl;

import org.junit.Assert;
import org.junit.Test;

public class LinksCleanerTest {

  private LinksCleaner cleaner = new LinksCleaner();

  @Test
  public void testCleanFullUrls() throws Exception {
    String MAIN_COMPARE = "RT @serpentskiss3: Residents In Russia Backed-Held" +
      " Ukraine Regret Separatist Drive - #RussiaInvadedUkraine ";

    String v = MAIN_COMPARE + "http://t.co/y9vONKUSGN";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanFullUrls(v));

    v = MAIN_COMPARE + "http:/";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanFullUrls(v));
  }

  @Test
  public void testCleanEndUrls() throws Exception {
    String MAIN_COMPARE = "RT @serpentskiss3: Residents In Russia Backed-Held" +
      " Ukraine Regret Separatist Drive - #RussiaInvadedUkraine ";

    String v = MAIN_COMPARE + "https:/";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "http:/!?";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "http://t.co...";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "htt,";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "http:";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "https:";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "http";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "http\t";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "http ";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "http. ";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "http?   \t\n\t   ";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "https";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "htt";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "ht";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "h";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "h.";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "h!";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "h...";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "h,";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));

    v = MAIN_COMPARE + "h-";
    Assert.assertEquals(MAIN_COMPARE, cleaner.cleanEndUrls(v));
  }
}