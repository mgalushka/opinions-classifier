package com.maximgalushka.driller;

import com.maximgalushka.classifier.twitter.LocalSettings;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;

public class DrillerImplTest {

  private final static Logger log = LoggerFactory.getLogger(
    DrillerImplTest.class
  );

  @Test
  public void testResolve() throws Exception {
    LocalSettings settings = new LocalSettings();
    settings.init();
    boolean integration = Boolean.valueOf(settings.value(LocalSettings.INTEGRATION_TESTING));

    if (!integration) {
      Driller driller = new DrillerImpl();
      Assert.assertEquals(
        driller.resolve("http://t.co/59WJLfWk3y"),
        driller.resolve("http://trib.al/ypTW4Te")
      );
    } else {
      log.warn("Ignoring Driller test because of integration testing mode.");
    }
  }
}