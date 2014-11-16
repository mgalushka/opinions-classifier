package com.maximgalushka.classifier.storage;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Maxim Galushka
 */
public class StorageServiceTest {
    public static final Logger log = Logger.getLogger(StorageServiceTest.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext ac =
                new ClassPathXmlApplicationContext(
                        "spring/classifier-services.xml"
                );
        StorageService ss = (StorageService) ac.getBean("storage");
        log.debug(ss.findClusters(30 * 60 * 1000));

        ConfigurableApplicationContext configurable =
                (ConfigurableApplicationContext) ac;
        configurable.close();
        System.exit(0);
    }
}
