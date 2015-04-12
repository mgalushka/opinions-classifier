package com.maximgalushka.classifier.twitter.service;

import com.google.gson.Gson;
import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import java.io.PrintStream;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
public class TwitterEasyApi implements Container {

  public static final Logger log = Logger.getLogger(TwitterEasyApi.class);

  private final Gson gson;

  protected LocalSettings settings;
  private StorageService storage;

  public void setStorage(StorageService storage) {
    this.storage = storage;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setSettings(LocalSettings settings) {
    this.settings = settings;
  }

  public TwitterEasyApi() {
    this.gson = new Gson();
  }

  public void headers(Response response) {
    long time = System.currentTimeMillis();
    response.setValue("Content-Type", "application/json");
    response.setValue("Server", "Tweeter Easy API");
    response.setDate("Date", time);
    response.setDate("Last-Modified", time);
    /** I leave it commented here as a reference for future
     This is fixed with proper apache server reverse proxy configuration
     <VirtualHost *:80>
     ProxyPreserveHost On
     ProxyRequests Off
     ServerName host
     ServerAlias host
     ProxyPass /easy http://localhost:8092
     ProxyPassReverse /easy http://localhost:8092
     </VirtualHost>
     */
    response.setValue("Access-Control-Allow-Origin", "*");
  }

  @Override
  public void handle(
    Request request, Response response
  ) {
    try {
      headers(response);
      PrintStream body = response.getPrintStream();

      String action = request.getPath().getName();
      Long tweetId = Long.parseLong(request.getParameter("tweetId"));
      String text = request.getParameter("text");
      if("retweet".equals(action)){
        Tweet original = storage.getTweetById(tweetId);
        Tweet retweet = new Tweet(tweetId, original.getText());
        storage.scheduleTweet(retweet, original, true);
      }
      if("update".equals(action)){
        Tweet original = storage.getTweetById(tweetId);
        Tweet updated = new Tweet(tweetId, text);
        storage.scheduleTweet(updated, original, false);
      }
      if("delete".equals(action)){
        storage.unpublishTweetCluster(tweetId);
      }
      body.println(gson.toJson("OK"));
      body.close();
      log.trace("Response sent");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
