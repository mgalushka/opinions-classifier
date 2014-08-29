package com.maximgalushka.classifier.twitter.service;

import org.apache.log4j.Logger;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;

import java.net.URI;

/**
 * @since 8/29/2014.
 */
public class MainServiceStart {

    public static final Logger log = Logger.getLogger(MainServiceStart.class);
    public static final String URL = "http://localhost:8090/topics";

    public static void main(String[] args) {
        JdkHttpServerFactory.createHttpServer(URI.create(URL), new TwitterTopicsService());
        log.debug(String.format("Twitter topics service started on: [%s]", URL));
    }
}
