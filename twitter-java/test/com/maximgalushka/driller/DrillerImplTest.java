package com.maximgalushka.driller;

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
    Driller driller = new DrillerImpl();
    Assert.assertEquals(
      driller.resolve("http://t.co/59WJLfWk3y"),
      driller.resolve("http://trib.al/ypTW4Te")
    );
  }
}