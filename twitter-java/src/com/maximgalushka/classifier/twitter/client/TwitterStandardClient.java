package com.maximgalushka.classifier.twitter.client;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.model.Media;
import com.maximgalushka.classifier.twitter.model.Statuses;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.maximgalushka.classifier.twitter.model.TwitterOAuthToken;
import com.maximgalushka.driller.Driller;
import com.maximgalushka.http.HttpHelper;
import com.sun.scenario.Settings;
import com.twitter.hbc.BasicRateTracker;
import com.twitter.hbc.BasicReconnectionManager;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import twitter4j.*;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("ALL")
public class TwitterStandardClient implements StreamClient {

  public static final String PROXY_ADDRESS = "http://localhost:4545";
  private Gson gson;
  private LocalSettings settings;
  private Driller driller;

  private boolean underTest = false;
  private boolean useProxy = false;

  public TwitterStandardClient() {
    this.gson = new Gson();
  }

  @PostConstruct
  private void init() {
    String ut = settings.value(LocalSettings.INTEGRATION_TESTING);
    if (ut != null) {
      this.underTest =
        Boolean.valueOf(settings.value(LocalSettings.INTEGRATION_TESTING));
      this.useProxy =
        Boolean.parseBoolean(settings.value(LocalSettings.USE_PROXY));
    }
  }

  public void setSettings(LocalSettings settings) {
    this.settings = settings;
  }

  public void setDriller(Driller driller) {
    this.driller = driller;
  }

  /**
   * @return bearer access token for application only by its secret details
   */
  private String bearer(String tokenKey, String tokenSecret) {
    if (underTest) {
      return testingStub("TESTING_OAUTH_KEY");
    }
    Client client = proxyHttpClient();

    WebTarget target = client.target("https://api.twitter.com/oauth2/token");
    WebTarget callTarget = target.queryParam(
      "grant_type",
      "client_credentials"
    );

    Invocation.Builder invocationBuilder = callTarget.request();

    invocationBuilder.header(
      "Content-Type",
      "application/x-www-form-urlencoded;charset=UTF-8"
    );

    String secret = String.format(
      "%s:%s",
      tokenKey,
      tokenSecret
    );
    String encoded = new String(Base64.encodeBase64(secret.getBytes()));
    invocationBuilder.header(
      "Authorization", String.format(
        "Basic %s",
        encoded
      )
    );

    Response response = invocationBuilder.post(
      Entity.entity(
        null,
        MediaType.TEXT_PLAIN_TYPE
      )
    );
    String json = response.readEntity(String.class);

    return gson.fromJson(json, TwitterOAuthToken.class).getAccessToken();
  }

  /**
   * @return bearer access token for application
   * @see <a href='https://dev.twitter
   * .com/oauth/reference/post/oauth/access_token'>
   * https://dev.twitter.com/oauth/reference/post/oauth/access_token
   * </a>
   */
  public String bearer() {
    return bearer(
      Settings.get(LocalSettings.CONSUMER_KEY),
      Settings.get(LocalSettings.CONSUMER_SECRET)
    );
  }

  /**
   * Searches updates on twitter sinse sinceId tweet id.
   *
   * @param token   twitter bearer token
   * @param query   query streang to search for updates
   * @param sinceId latest id we already have results from
   * @return list of tweets which were sent on the query since sinceId
   */
  public Statuses search(String token, String query, long sinceId) {
    if (underTest) {
      return testingStub(new Statuses());
    }
    Client client = proxyHttpClient();
    WebTarget search = client.target(
      "https://api.twitter.com/1.1/search/tweets.json"
    );
    WebTarget callTarget = search
      .queryParam("q", query)               // maximum length = 500
      .queryParam("count", 100)             // maximum = 100
      .queryParam("since_id", sinceId)
      .queryParam("result_type", "recent")
      .queryParam("include_entities", 1)
      .queryParam("lang", "en");

    Invocation.Builder invocationBuilder = callTarget.request();

    invocationBuilder.header(
      "Content-Type",
      "application/x-www-form-urlencoded;charset=UTF-8"
    );
    invocationBuilder.header(
      "Authorization",
      String.format(
        "Bearer %s",
        token
      )
    );

    Response response = invocationBuilder.get();
    String json = response.readEntity(String.class);

    return gson.fromJson(json, Statuses.class);
  }

  /**
   * Reads messages from twitter streaming API and publishes to queue
   * Based on https://github.com/twitter/hbc project
   */
  @Override
  public void stream(String term, BlockingQueue<Tweet> output) {
    if (underTest) {
      return;
    }
    // Set up your blocking queues: Be sure to size these properly based on
    // expected TPS of your stream
    BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

    // Declare the host you want to connect to, the endpoint, and
    // authentication (basic auth or oauth)
    Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
    StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
    // Optional: set up some followings and track terms
    //List<Long> followings = Lists.newArrayList(1234L, 566788L);
    List<String> terms = Arrays.asList(term.split(","));
    List<String> languages = Lists.newArrayList("en");
    //hosebirdEndpoint.followings(followings);
    hosebirdEndpoint.trackTerms(terms);
    hosebirdEndpoint.languages(languages);
    hosebirdEndpoint.delimited(true);

    // These secrets should be read from a config file
    Authentication hosebirdAuth = new OAuth1(
      settings.value(LocalSettings.CONSUMER_KEY),
      settings.value(LocalSettings.CONSUMER_SECRET),
      settings.value(LocalSettings.ACCESS_TOKEN),
      settings.value(LocalSettings.ACCESS_TOKEN_SECRET)
    );

    com.twitter.hbc.ClientBuilder builder = new com.twitter.hbc.ClientBuilder()
      .name("Hosebird-Client-01")                              // optional:
        // mainly for the logs
      .hosts(hosebirdHosts)

      .authentication(hosebirdAuth)
      .endpoint(hosebirdEndpoint)
      .processor(new TweetStringDelimeterProcessor(output))
      .eventMessageQueue(eventQueue);                          // optional:
    // use this if you want to process client events

    HttpParams params = new BasicHttpParams();

    if (useProxy) {
      HttpHost proxy = new HttpHost("localhost", 4545);
      params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
    SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();
    BasicReconnectionManager reconnectionManager =
      new BasicReconnectionManager(5);
    BasicRateTracker rateTracker = new BasicRateTracker(
      30000,
      100,
      true,
      scheduled
    );
    BasicClient client = new BasicClient(
      "TwitterStreamClient",
      hosebirdHosts,
      hosebirdEndpoint,
      hosebirdAuth,
      true,
      new TweetStringDelimeterProcessor(output),
      reconnectionManager,
      rateTracker,
      executorService,
      eventQueue,
      params,
      schemeRegistry
    );

    // start streaming
    client.connect();
  }

  /**
   * @param tweetId tweet to re-tweet from user account
   */
  public Status retweet(long tweetId) throws TwitterException {
    if (underTest) {
      return testingStub(null);
    }

    Twitter twitter = TwitterFactory.getSingleton();
    return twitter.retweetStatus(tweetId);
  }

  public Status post(
    Tweet tweet,
    boolean attachUrl,
    boolean attachImage
  )
  throws Exception {
    if (underTest) {
      return testingStub(null);
    }

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpHelper helper = new HttpHelper(httpclient);
    Twitter twitter = TwitterFactory.getSingleton();

    String updateText = tweet.getText();
    if (attachUrl) {
      // TODO: hard-coded max URL length.
      if (updateText.length() <= (140 - 24) &&
        tweet.getEntities().getUrls() != null &&
        !tweet.getEntities().getUrls().isEmpty()) {
        String firstUrl = tweet.getEntities().getUrls().get(0).getUrl();
        String resolved = driller.resolve(firstUrl);
        updateText = String.format(
          "%s %s",
          tweet.getText(),
          resolved
        );
      }
    }
    StatusUpdate update = new StatusUpdate(updateText);

    if (attachImage) {
      if (tweet.getEntities().getMedia() != null &&
        !tweet.getEntities().getMedia().isEmpty()) {
        for (Media media : tweet.getEntities().getMedia()) {
          File temp = File.createTempFile("download_", "");
          // TODO: rethink - as resources are not indefinite
          // TODO: track where attached fiels are stored
          // TODO: temp.deleteOnExit();
          helper.download(media.getUrl(), temp);
          update.setMedia(temp);
        }
      }
    }

    return twitter.updateStatus(update);
  }

  private Client proxyHttpClient() {
    ClientConfig cc = new ClientConfig();
    if (this.useProxy) {
      cc.property(ClientProperties.PROXY_URI, PROXY_ADDRESS);
      cc.connectorProvider(new ApacheConnectorProvider());
    }
    return ClientBuilder.newClient(cc);
  }

  private <T> T testingStub(T result) {
    return result;
  }
}
