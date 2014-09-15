package com.maximgalushka.classifier.twitter;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Maxim Galushka
 */
public class LocalSettings {

    public static final String CONSUMER_KEY = "consumer.key";
    public static final String CONSUMER_SECRET = "consumer.secret";
    public static final String ACCESS_TOKEN = "access.token";
    public static final String ACCESS_TOKEN_SECRET = "access.token.secret";

    public static final String USE_PROXY = "useproxy";

    public static final String MEMCACHED_HOST = "memcached.host";
    public static final String MEMCACHED_PORT = "memcached.port";

    private Properties properties;
    private static final LocalSettings settings = new LocalSettings();

    private LocalSettings() {
        try {
            properties = new Properties();
            properties.load(LocalSettings.class.getResourceAsStream("/details.properties"));
        } catch (IOException e) {

        }
    }

    public static LocalSettings settings() {
        return settings;
    }

    public String value(String key) {
        return String.valueOf(properties.get(key));
    }
}
