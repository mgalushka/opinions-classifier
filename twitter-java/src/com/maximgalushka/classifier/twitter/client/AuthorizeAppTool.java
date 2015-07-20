package com.maximgalushka.classifier.twitter.client;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.account.TwitterAccount;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.PropertyConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Tool used to authorize app to read/write to user account.
 *
 * @author Maxim Galushka
 */
public class AuthorizeAppTool {

  public static void main(String[] args) {
    Long accountId = Long.parseLong(args[0]);

    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );

    StorageService storage = (StorageService) ac.getBean("storage");
    TwitterAccount account = storage.getAccountById(accountId);
    if (account == null) {
      throw new RuntimeException("Account not found");
    }

    Properties props = new Properties();
    props.put(LocalSettings.OAUTH_CONSUMER_KEY, account.getConsumerKey());
    props.put(LocalSettings.OAUTH_CONSUMER_SECRET, account.getConsumerSecret());

    TwitterFactory tf = new TwitterFactory(new PropertyConfiguration(props));
    Twitter twitter = tf.getInstance();

    RequestToken requestToken = null;
    try {
      requestToken = twitter.getOAuthRequestToken();
    } catch (Exception e) {
      System.out.println("Are the consumer key and secret correct?");
      e.printStackTrace();
    }

    assert requestToken != null;
    System.out.println(requestToken.getAuthorizationURL());
    System.out.print("Please enter the PIN from Twitter: ");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String pin = null;
    try {
      pin = br.readLine();
    } catch (IOException e) {
      System.out.println("I guess you don't want to?");
      e.printStackTrace();
      System.exit(-1);
    }
    AccessToken token = null;
    try {
      token = twitter.getOAuthAccessToken(requestToken, pin);
    } catch (TwitterException e) {
      System.out.println("Was there a typo in the PIN you entered?");
      e.printStackTrace();
      System.exit(-1);
    }
    twitter.setOAuthAccessToken(token);

    System.out.printf(
      "Token key=%s secret=%s\n",
      token.getToken(),
      token.getTokenSecret()
    );

    storage.updateUserAccountToken(
      accountId,
      token.getToken(),
      token.getTokenSecret()
    );

    System.exit(0);
  }
}
