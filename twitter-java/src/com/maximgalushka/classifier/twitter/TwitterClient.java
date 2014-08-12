package com.maximgalushka.classifier.twitter;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.model.Statuses;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.maximgalushka.classifier.twitter.model.TwitterOAuthToken;
import com.twitter.hbc.BasicRateTracker;
import com.twitter.hbc.BasicReconnectionManager;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Maxim Galushka
 */
public class TwitterClient {

    public static final String PROXY_ADDRESS = "http://localhost:4545";
    private Gson gson;
    private TwitterSettings settings = new TwitterSettings();

    public TwitterClient() {
        this.gson = new Gson();
    }

    /**
     * @return access token
     */
    public String oauth() {
        Client client = proxyHttpClient();

        WebTarget target = client.target("https://api.twitter.com/oauth2/token");
        WebTarget callTarget = target.queryParam("grant_type", "client_credentials");

        Invocation.Builder invocationBuilder = callTarget.request();

        invocationBuilder.header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        String secret = String.format("%s:%s",
                settings.value(TwitterSettings.CONSUMER_KEY), settings.value(TwitterSettings.CONSUMER_SECRET));
        String encoded = new String(Base64.encodeBase64(secret.getBytes()));
        invocationBuilder.header("Authorization", String.format("Basic %s", encoded));

        Response response = invocationBuilder.post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));
        String json = response.readEntity(String.class);

        return gson.fromJson(json, TwitterOAuthToken.class).getAccessToken();
    }

    public List<Tweet> search(String token, String query) {
        Client client = proxyHttpClient();
        WebTarget search = client.target("https://api.twitter.com/1.1/search/tweets.json");
        WebTarget callTarget = search.queryParam("q", query).queryParam("count", 200).queryParam("lang", "en");

        Invocation.Builder invocationBuilder = callTarget.request();

        invocationBuilder.header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        invocationBuilder.header("Authorization", String.format("Bearer %s", token));

        Response response = invocationBuilder.get();
        String json = response.readEntity(String.class);

        return gson.fromJson(json, Statuses.class).getTweets();
    }

    /**
     * Taken from https://github.com/twitter/hbc project
     */
    @SuppressWarnings("deprecation")
    public BasicClient stream(BlockingQueue<String> output) {
        // Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream
        //BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

        // Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth)
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        List<String> terms = Lists.newArrayList("ukraine");
        List<String> languages = Lists.newArrayList("en");
        //hosebirdEndpoint.followings(followings);
        hosebirdEndpoint.trackTerms(terms);
        hosebirdEndpoint.languages(languages);
        hosebirdEndpoint.delimited(true);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(
                settings.value(TwitterSettings.CONSUMER_KEY), settings.value(TwitterSettings.CONSUMER_SECRET),
                settings.value(TwitterSettings.ACCESS_TOKEN), settings.value(TwitterSettings.ACCESS_TOKEN_SECRET));

        com.twitter.hbc.ClientBuilder builder = new com.twitter.hbc.ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)

                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(output))
                .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

        HttpHost proxy = new HttpHost("localhost", 4545);
        HttpParams params = new BasicHttpParams();
        params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();
        BasicReconnectionManager reconnectionManager = new BasicReconnectionManager(5);
        BasicRateTracker rateTracker = new BasicRateTracker(30000, 100, true, scheduled);
        return new BasicClient("TwitterStreamClient",
                hosebirdHosts, hosebirdEndpoint, hosebirdAuth, true, new StringDelimitedProcessor(output),
                reconnectionManager, rateTracker, executorService, eventQueue, params, schemeRegistry);
    }

    private Client proxyHttpClient() {
        ClientConfig cc = new ClientConfig();
        cc.property(ClientProperties.PROXY_URI, PROXY_ADDRESS);
        cc.connectorProvider(new ApacheConnectorProvider());
        return ClientBuilder.newClient(cc);
    }
}
