package com.maximgalushka.classifier.twitter.classify.carrot;

import org.junit.Assert;
import org.junit.Test;

public class ClusteringTweetsListTest {

    private ClusteringTweetsList cleaner = ClusteringTweetsList.getAlgorithm();

    @Test
    public void testReformatMessage() throws Exception {
        Assert.assertEquals("", cleaner.reformatMessage("rt Rt"));
        Assert.assertEquals("", cleaner.reformatMessage("rt"));
        Assert.assertEquals("", cleaner.reformatMessage("RT"));
        Assert.assertEquals("", cleaner.reformatMessage("Rt"));
        Assert.assertEquals("", cleaner.reformatMessage("rT"));

        Assert.assertEquals("", cleaner.reformatMessage("@test.mention"));
        Assert.assertEquals("", cleaner.reformatMessage("@test._mention"));
        Assert.assertEquals("", cleaner.reformatMessage("@test-mention"));
        Assert.assertEquals("", cleaner.reformatMessage(" @testmention "));
        Assert.assertEquals("", cleaner.reformatMessage(" @test123#$%^&*()mention "));
        Assert.assertEquals("normal text", cleaner.reformatMessage("@test!@@@@mention @test @ttt0008767677*7&& normal text"));

        Assert.assertEquals("Jonny told", cleaner.reformatMessage("Jonny told http://google.com"));
        Assert.assertEquals("Jonny told", cleaner.reformatMessage("Jonny told http://"));
        Assert.assertEquals("Jonny told", cleaner.reformatMessage("Jonny told http:/"));

        Assert.assertEquals("Grigoriy told", cleaner.reformatMessage("Grigoriy   told https://google.com"));
        Assert.assertEquals("Petya told", cleaner.reformatMessage("Petya         told        https://"));
        Assert.assertEquals("Ivan told", cleaner.reformatMessage("      Ivan \n\n\n\t\t\t   told https:/        "));


        Assert.assertEquals("Grigoriy told ht htt htt:", cleaner.reformatMessage("Grigoriy   told https://google.com ht htt http htt:"));
    }

    @Test
    public void testJaccard() throws Exception {
        Assert.assertEquals(1, cleaner.jaccard("a b c", "a b c"), 0.00001);
        Assert.assertEquals(0.6666666666, cleaner.jaccard("b c", "a b c"), 0.00001);
        Assert.assertEquals(0, cleaner.jaccard("d", "a b c"), 0.00001);
    }
}