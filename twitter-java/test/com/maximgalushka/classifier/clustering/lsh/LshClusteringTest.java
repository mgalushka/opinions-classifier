package com.maximgalushka.classifier.clustering.lsh;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LshClusteringTest {

  public static final Logger log = Logger.getLogger(LshClusteringTest.class);

  private Set<String> same = new HashSet<>(
    Arrays.asList(
      "S&P downgrades Ukraine credit rating by one notch to CC #AcehCenterID",
      "3Novices:S&P downgrades Ukraine credit rating by one notch to CC " +
        "Ratings" +
        " firm Standard & Poor's cut Ukraine's cred",
      "S&P downgrades Ukraine credit rating by one notch to CC",
      "S&P downgrades Ukraine credit rating by one notch to CC #business #sg",
      "S&P downgrades Ukraine credit rating by one notch to CC #HeadlinesApp"
    )
  );


  @Test
  public void testProccessClusterIds() throws Exception {
    LshClustering clustering = new LshClustering();
    Set<String> result = clustering.proccessClusterIds(same);
    log.debug(result);
  }
}