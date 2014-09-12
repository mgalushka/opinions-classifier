package com.maximgalushka.classifier.storage.memcached;

import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Maxim Galushka
 */
public class MemClient {

    public static final Logger log = Logger.getLogger(MemClient.class);

    public static void main(String[] args) throws IOException {
        MemcachedClient c = new MemcachedClient(
                new InetSocketAddress("ec2-54-68-39-246.us-west-2.compute.amazonaws.com",
                        11211));

        // Store a value (async) for one hour
        c.set("someKey", 3600, "someObject");
        // Retrieve a value (synchronously).
        Object myObject = c.get("someKey");

        log.debug(myObject);
    }
}
