package com.maximgalushka.classifier.twitter.classify.carrot;

import com.maximgalushka.classifier.twitter.classify.TextCleanup;
import com.maximgalushka.classifier.twitter.classify.Tools;
import org.junit.Assert;
import org.junit.Test;

public class ClusteringTweetsListTest {

  private TextCleanup cleaner = new TextCleanup();

  @Test
  public void testReformatMessage() throws Exception {
    Assert.assertEquals("", TextCleanup.reformatMessage("rt Rt"));
    Assert.assertEquals("", TextCleanup.reformatMessage("rt"));
    Assert.assertEquals("", TextCleanup.reformatMessage("RT"));
    Assert.assertEquals("", TextCleanup.reformatMessage("Rt"));
    Assert.assertEquals("", TextCleanup.reformatMessage("rT"));

    Assert.assertEquals("", TextCleanup.reformatMessage("@test.mention"));
    Assert.assertEquals("", TextCleanup.reformatMessage("@test._mention"));
    Assert.assertEquals("", TextCleanup.reformatMessage("@test-mention"));
    Assert.assertEquals("", TextCleanup.reformatMessage(" @testmention "));
    Assert.assertEquals(
      "", TextCleanup.reformatMessage(
        " @test123#$%^&*()mention "
      )
    );
    Assert.assertEquals(
      "normal text", TextCleanup.reformatMessage(
        "@test!@@@@mention @test @ttt0008767677*7&& " +
          "normal text"
      )
    );

    Assert.assertEquals(
      "Jonny told", TextCleanup.reformatMessage(
        "Jonny told http://google.com"
      )
    );
    Assert.assertEquals(
      "Jonny told", TextCleanup.reformatMessage(
        "Jonny told http://"
      )
    );
    Assert.assertEquals(
      "Jonny told", TextCleanup.reformatMessage(
        "Jonny told http:/"
      )
    );

    Assert.assertEquals(
      "Grigoriy told", TextCleanup.reformatMessage(
        "Grigoriy   told https://google.com"
      )
    );
    Assert.assertEquals(
      "Petya told", TextCleanup.reformatMessage(
        "Petya         told        https://"
      )
    );
    Assert.assertEquals(
      "Ivan told", TextCleanup.reformatMessage(
        "      Ivan \n\n\n\t\t\t   told https:/        "
      )
    );


    Assert.assertEquals(
      "Grigoriy told ht htt htt:", TextCleanup.reformatMessage(
        "Grigoriy   told https://google.com ht htt http htt:"
      )
    );
  }

  @Test
  public void testJaccard() throws Exception {
    Assert.assertEquals(1, Tools.jaccard("a b c", "a b c"), 0.00001);
    Assert.assertEquals(0.6666666666, Tools.jaccard("b c", "a b c"), 0.00001);
    Assert.assertEquals(0, Tools.jaccard("d", "a b c"), 0.00001);
  }
}