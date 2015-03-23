package com.maximgalushka.classifier.twitter.cleanup.impl;

import org.junit.Assert;
import org.junit.Test;

public class RetweetCleanerTest {

  private RetweetCleaner cleaner = new RetweetCleaner();

  @Test
  public void testClean() throws Exception {
    String MAIN_COMPARE = " Residents In Russia Backed-Held" +
      " Ukraine Regret Separatist Drive - #RussiaInvadedUkraine ";

    String v = "RT @serpentskiss3:" + MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v = "\trt\t@s:" + MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v = " rT @s:" + MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v = " Rt @s:" + MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));
  }
}