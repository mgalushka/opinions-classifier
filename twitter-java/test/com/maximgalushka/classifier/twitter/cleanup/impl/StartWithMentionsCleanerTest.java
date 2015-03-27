package com.maximgalushka.classifier.twitter.cleanup.impl;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class StartWithMentionsCleanerTest {

  private StartWithMentionsCleaner cleaner = new StartWithMentionsCleaner();

  @Test
  public void testClean() throws Exception {
    String MAIN_COMPARE = " Residents In Russia Backed-Held" +
      " Ukraine Regret Separatist Drive - #RussiaInvadedUkraine ";

    String v = "@serpentskiss3:" + MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v ="@serpentskiss3, @popandopalo:" +  MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v ="@serpentskiss3,@popandopalo:" +  MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v =" @serpentskiss3 @popandopalo:" +  MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));

    v ="\n\t@serpentskiss3, \n\r\t@popandopalo:" +  MAIN_COMPARE;
    Assert.assertEquals(MAIN_COMPARE, cleaner.clean(v));
  }
}