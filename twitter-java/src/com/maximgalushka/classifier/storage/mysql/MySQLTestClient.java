package com.maximgalushka.classifier.storage.mysql;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Maxim Galushka
 */
public class MySQLTestClient {

    public static final Logger log = Logger.getLogger(MySQLTestClient.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext ac =
                new ClassPathXmlApplicationContext(
                        "spring/classifier-services.xml"
                );
        MysqlService ss = (MysqlService) ac.getBean("mysql");
        log.debug(ss.loadClusters(30 * 60 * 1000));

        ConfigurableApplicationContext configurable =
                (ConfigurableApplicationContext) ac;
        configurable.close();
        System.exit(0);
    }
}
