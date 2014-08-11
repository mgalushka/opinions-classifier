package com.maximgalushka.classifier.twitter;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Maxim Galushka
 */
public class TwitterSettings {

    public static final String CONSUMER_KEY = "consumer.key";
    public static final String CONSUMER_SECRET = "consumer.secret";
    public static final String ACCESS_TOKEN = "access.token";
    public static final String ACCESS_TOKEN_SECRET = "access.token.secret";

    private Properties properties;

    public TwitterSettings() {
        try {
            properties = new Properties();
            properties.load(TwitterSettings.class.getResourceAsStream("/details.properties"));
        } catch (IOException e) {

        }
    }

    public String value(String key) {
        return String.valueOf(properties.get(key));
    }
}
