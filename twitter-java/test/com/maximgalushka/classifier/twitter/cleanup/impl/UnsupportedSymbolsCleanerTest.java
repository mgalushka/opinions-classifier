package com.maximgalushka.classifier.twitter.cleanup.impl;

import org.junit.Assert;
import org.junit.Test;

public class UnsupportedSymbolsCleanerTest {

  private UnsupportedSymbolsCleaner cleaner = new UnsupportedSymbolsCleaner();

  @Test
  public void testClean() throws Exception {
    String MAIN_COMPARE = "Residents In Russia%s Backed-Held" +
      " Ukraine Regret Separatist Drive - #RussiaInvadedUkraine";

    Assert.assertEquals(
      String.format(MAIN_COMPARE, ""),
      cleaner.clean(
        String.format(MAIN_COMPARE, "?????")
      )
    );
  }
}