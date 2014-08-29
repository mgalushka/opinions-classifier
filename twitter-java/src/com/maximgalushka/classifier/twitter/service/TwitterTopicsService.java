package com.maximgalushka.classifier.twitter.service;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @since 8/29/2014.
 */
@ApplicationPath("/")
public class TwitterTopicsService extends ResourceConfig {

    public static final Logger log = Logger.getLogger(TwitterTopicsService.class);

    public TwitterTopicsService() {
        log.debug("Loaded packages");
        packages("...");
    }

    @GET
    @Produces("text/plain")
    public Response foo() {
        log.debug("Requested");
        return Response.ok("Hey, it's working!\n").build();
    }
}

