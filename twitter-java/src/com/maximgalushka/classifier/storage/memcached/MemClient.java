package com.maximgalushka.classifier.storage.memcached;

import com.maximgalushka.classifier.twitter.LocalSettings;
import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Maxim Galushka
 */
public class MemClient {

    public static final Logger log = Logger.getLogger(MemClient.class);

    private static final LocalSettings settings = LocalSettings.settings();

    public static void main(String[] args) throws IOException {
        MemcachedClient c = new MemcachedClient(
                new InetSocketAddress(settings.value(LocalSettings.MEMCACHED_HOST),
                        Integer.valueOf(settings.value(LocalSettings.MEMCACHED_PORT))));

        // Store a value (async) for one hour
        c.set("someKey", 3600, "someObject");
        // Retrieve a value (synchronously).
        Object myObject = c.get("someKey");

        log.debug(myObject);


    }
}
