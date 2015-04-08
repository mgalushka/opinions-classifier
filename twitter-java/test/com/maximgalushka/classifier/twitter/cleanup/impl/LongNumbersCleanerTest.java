package com.maximgalushka.classifier.twitter.cleanup.impl;

import org.junit.Assert;
import org.junit.Test;

public class LongNumbersCleanerTest {

  private LongNumbersCleaner cleaner = new LongNumbersCleaner();

  @Test
  public void testCleanLongNumbers() throws Exception {
    String MAIN_COMPARE = "RT @serpentskiss3: Residents In Russia Backed-Held" +
      " Ukraine Regret Separatist Drive - #RussiaInvadedUkraine ";

    String v = MAIN_COMPARE + "1234";
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v = MAIN_COMPARE + "54321";
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v = "Some tweet 1234 text with 5431 numbers inside";
    Assert.assertEquals(
      "Some tweet  text with  numbers inside",
      cleaner.clean(v)
    );
  }
}