package com.maximgalushka.classifier.storage.memcached;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Maxim Galushka
 */
public class MemTestingClient {

    public static final Logger log = Logger.getLogger(MemTestingClient.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext ac =
                new ClassPathXmlApplicationContext(
                        "spring/classifier-services.xml"
                );
        MemcachedService ss = (MemcachedService) ac.getBean("memcached");
        /*
        Clusters add = new Clusters();
        add.addClusters(Arrays.asList(
                new Cluster(1, "test", "some message", 70,
                        "http://google.com", "http://imgur.com")));
        long latest = ss.createNewClustersGroup(add);
        log.debug(String.format("Added new clusters group for [%d]", latest));
        */
        log.debug(ss.mergeFromTimestamp(30 * 60 * 1000));
        ConfigurableApplicationContext configurable =
                (ConfigurableApplicationContext) ac;
        configurable.close();
        System.exit(0);
    }
}
