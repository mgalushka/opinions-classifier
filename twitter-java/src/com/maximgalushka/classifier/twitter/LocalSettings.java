package com.maximgalushka.classifier.twitter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
@Resource(name = "settings")
public class LocalSettings {

  public static final String CONSUMER_KEY = "consumer.key";
  public static final String CONSUMER_SECRET = "consumer.secret";
  public static final String ACCESS_TOKEN = "access.token";
  public static final String ACCESS_TOKEN_SECRET = "access.token.secret";

  public static final String USER_CONSUMER_KEY = "user.consumer.key";
  public static final String USER_CONSUMER_SECRET = "user.consumer.secret";
  public static final String USER_ACCESS_TOKEN = "user.access.token";
  public static final String USER_ACCESS_TOKEN_SECRET =
    "user.access.token.secret";

  public static final String USE_PROXY = "useproxy";
  public static final String INTEGRATION_TESTING = "integration.testing";

  public static final String EASY_WEB_PORT = "easy.web.port";
  public static final String WEB_PORT = "service.web.port";
  public static final String SHUTDOWN_PORT = "service.shutdown.port";

  public static final String TWITTER_KEYWORDS = "twitter.monitor.keywords";

  public static final String MEMCACHED_HOST = "memcached.host";
  public static final String MEMCACHED_PORT = "memcached.port";

  public static final String MYSQL_URL = "mysql.url";
  public static final String MYSQL_USERNAME = "mysql.username";
  public static final String MYSQL_PASSWORD = "mysql.password";

  private Properties properties;
  private static final LocalSettings settings = new LocalSettings();

  public LocalSettings() {
  }

  @PostConstruct
  public void init() throws IOException {
    properties = new Properties();
    properties.load(
      LocalSettings.class.getResourceAsStream("/details.properties")
    );
  }

  public String value(String key) {
    Object value = properties.get(key);
    if (value == null) {
      return null;
    }
    return String.valueOf(value);
  }
}
