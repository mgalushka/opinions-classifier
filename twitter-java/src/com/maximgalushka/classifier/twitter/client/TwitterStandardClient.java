package com.maximgalushka.classifier.twitter.client;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.account.TwitterAccount;
import com.maximgalushka.classifier.twitter.model.*;
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
import org.apache.log4j.Logger;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.PropertyConfiguration;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("ALL")
public class TwitterStandardClient implements StreamClient {

  public static final Logger log = Logger.getLogger(
      TwitterStandardClient.class
  );

  public static final String PROXY_ADDRESS = "http://localhost:4545";
  private Gson gson;
  private ExecutorService executorService;
  private ScheduledExecutorService scheduled;
  private LocalSettings settings;
  private Driller driller;

  private boolean underTest = false;
  private boolean useProxy = false;

  private BasicClient basicStreamClient;

  public TwitterStandardClient() {
    this.gson = new Gson();
    this.executorService = Executors.newFixedThreadPool(2);
    this.scheduled = Executors.newScheduledThreadPool(2);
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
  public void stream(
      TwitterAccount account,
      String term,
      BlockingQueue<Tweet> output
  ) {
    if (underTest) {
      return;
    }
    // Set up your blocking queues: Be sure to size these properly based on
    // expected TPS of your stream
    BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(1000);

    // Declare the host you want to connect to, the endpoint, and
    // authentication (basic auth or oauth)
    Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
    StatusesFilterEndpoint statusFilterEndpoint = new StatusesFilterEndpoint();
    // Optional: set up some followings and track terms
    //List<Long> followings = Lists.newArrayList(1234L, 566788L);
    List<String> terms = Arrays.asList(term.split(","));
    List<String> languages = Lists.newArrayList(account.getLanguage());
    //statusFilterEndpoint.followings(followings);
    statusFilterEndpoint.trackTerms(terms);
    statusFilterEndpoint.languages(languages);
    //statusFilterEndpoint.filterLevel(Constants.FilterLevel.None);
    statusFilterEndpoint.delimited(true);

    // These secrets should be read from a config file
    Authentication hosebirdAuth = new OAuth1(
        account.getConsumerKey(),
        account.getConsumerSecret(),
        account.getAccessToken(),
        account.getAccessTokenSecret()
    );

    SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();
    BasicReconnectionManager reconnectionManager =
        new BasicReconnectionManager(5);
    BasicRateTracker rateTracker = new BasicRateTracker(
        30000,
        100,
        true,
        this.scheduled
    );

    com.twitter.hbc.ClientBuilder builder = new com.twitter.hbc.ClientBuilder()
      .name(String.format("Hosebird-Client-%d", account.getId()))
      // optional: mainly for the logs
      .hosts(hosebirdHosts)
      .gzipEnabled(true)
      .authentication(hosebirdAuth)
      .endpoint(statusFilterEndpoint)
      .processor(new TweetStringDelimeterProcessor(account.getId(), output))
      .rateTracker(rateTracker)
      .reconnectionManager(reconnectionManager)
      .eventMessageQueue(eventQueue)
      .schemeRegistry(schemeRegistry);

    HttpParams params = new BasicHttpParams();

    if (useProxy) {
      HttpHost proxy = new HttpHost("localhost", 4545);
      params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    this.basicStreamClient = builder.build();

    /*
    this.basicStreamClient = new BasicClient(
        "TwitterStreamClient",
        hosebirdHosts,
        statusFilterEndpoint,
        hosebirdAuth,
        true,
        new TweetStringDelimeterProcessor(account.getId(), output),
        reconnectionManager,
        rateTracker,
        executorService,
        eventQueue,
        params,
        schemeRegistry
    );
    */


    // start streaming
    this.basicStreamClient.connect();
  }

  public void stopHosebird() {
    this.basicStreamClient.stop();
  }

  /**
   * Returns HTML representation of embedded tweet
   */
  public String embedded(
      TwitterAccount account,
      long tweetId,
      String tweetUrl
  ) throws TwitterException {
    if (underTest) {
      return testingStub(null);
    }
    if (account == null) {
      throw new NullPointerException(
          "Account passed cannot be null"
      );
    }
    Properties props = new Properties();
    props.put(LocalSettings.OAUTH_CONSUMER_KEY, account.getConsumerKey());
    props.put(LocalSettings.OAUTH_CONSUMER_SECRET, account.getConsumerSecret());

    TwitterFactory tf = new TwitterFactory(new PropertyConfiguration(props));
    Twitter twitter = tf.getInstance(
        new AccessToken(
            account.getUserAccessToken(),
            account.getUserAccessTokenSecret()
        )
    );
    OEmbedRequest embedRequest = new OEmbedRequest(tweetId, tweetUrl);
    embedRequest.setHideMedia(false);
    embedRequest.setMaxWidth(550);
    embedRequest.setHideThread(false);
    OEmbed oembed = twitter.getOEmbed(embedRequest);
    return oembed.getHtml();
  }

  /**
   * @param tweetId tweet to re-tweet from user account
   */
  public Status retweet(
      TwitterAccount account,
      long tweetId
  ) throws TwitterException {
    if (underTest) {
      return testingStub(null);
    }
    if (account == null) {
      throw new NullPointerException(
          "Account passed cannot be null"
      );
    }
    Properties props = new Properties();
    props.put(LocalSettings.OAUTH_CONSUMER_KEY, account.getConsumerKey());
    props.put(LocalSettings.OAUTH_CONSUMER_SECRET, account.getConsumerSecret());

    TwitterFactory tf = new TwitterFactory(new PropertyConfiguration(props));
    Twitter twitter = tf.getInstance(
        new AccessToken(
            account.getUserAccessToken(),
            account.getUserAccessTokenSecret()
        )
    );
    return twitter.retweetStatus(tweetId);
  }

  public Status post(
      TwitterAccount account,
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

    Properties props = new Properties();
    props.put(LocalSettings.OAUTH_CONSUMER_KEY, account.getConsumerKey());
    props.put(LocalSettings.OAUTH_CONSUMER_SECRET, account.getConsumerSecret());

    TwitterFactory tf = new TwitterFactory(new PropertyConfiguration(props));
    Twitter twitter = tf.getInstance(
        new AccessToken(
            account.getUserAccessToken(),
            account.getUserAccessTokenSecret()
        )
    );

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

  /**
   * Returns latest tweets from public account.
   * It will fail if account in not public.
   *
   * @param account twitter account to get tweets for
   * @return all latest tweets
   * @throws Exception
   * @deprecated re-think!
   */
  @Deprecated
  public List<Tweet> getLatestTweets(String account) throws Exception {
    if (underTest) {
      return testingStub(null);
    }
    try {
      Twitter twitter = new TwitterFactory().getInstance();
      return Lists.transform(
          getLatestStatuses(twitter, account),
          new StatusToTweetFunction()
      );
    } catch (Exception e) {
      log.warn(
          String.format(
              "Cannot extract statuses for %s account. " +
                  "Probably this is not public. Trying to extract via account itself",
              account
          )
      );
      throw e;
    }
  }

  /**
   * @param account user account to get latest tweets for
   * @return list of latest tweets posted
   * @throws Exception
   */
  public List<Tweet> getLatestTweets(TwitterAccount account)
      throws Exception {
    if (underTest) {
      return testingStub(null);
    }

    List<Tweet> statuses = Lists.newArrayList();
    try {
      Properties props = new Properties();
      props.put(LocalSettings.OAUTH_CONSUMER_KEY, account.getConsumerKey());
      props.put(
          LocalSettings.OAUTH_CONSUMER_SECRET,
          account.getConsumerSecret()
      );

      TwitterFactory tf = new TwitterFactory(new PropertyConfiguration(props));
      Twitter twitter = tf.getInstance(
          new AccessToken(
              account.getAccessToken(),
              account.getAccessTokenSecret()
          )
      );
      statuses = Lists.transform(
          getLatestStatuses(twitter, account.getAccount()),
          new StatusToTweetFunction()
      );
    } catch (Exception e) {
      log.error(
          String.format(
              "Cannot extract statuses for %s account.",
              account.getAccount()
          ),
          e
      );
    }
    return statuses;
  }

  private List<Status> getLatestStatuses(
      Twitter client,
      String twitterAccount
  ) throws Exception {
    Paging paging = new Paging(1, 200);
    return client.getUserTimeline(twitterAccount, paging);
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
