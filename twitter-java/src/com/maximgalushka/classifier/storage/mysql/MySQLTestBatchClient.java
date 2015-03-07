package com.maximgalushka.classifier.storage.mysql;

import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;

/**
 * @author Maxim Galushka
 */
public class MySQLTestBatchClient {

  public static final Logger log = Logger.getLogger(MySQLTestBatchClient.class);

  public static void main(String[] args) throws Exception {
    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );
    MysqlService ss = (MysqlService) ac.getBean("mysql");
    ss.saveTweetsBatch(
      Arrays.asList(
        new Tweet(1, "1"),
        new Tweet(2, "1"),
        new Tweet(2, "1"),
        new Tweet(3, "1"),
        new Tweet(4, "1"),
        new Tweet(4, "1"),
        new Tweet(4, "1"),
        new Tweet(7, "1"),
        new Tweet(5, "1"),
        new Tweet(6, "1")
      )
    );

    ConfigurableApplicationContext configurable =
      (ConfigurableApplicationContext) ac;
    configurable.close();
    System.exit(0);
  }
}
