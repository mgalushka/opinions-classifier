package com.maximgalushka.classifier.twitter;

import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.model.Statuses;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.maximgalushka.classifier.twitter.model.TwitterOAuthToken;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
        WebTarget callTarget = search.queryParam("q", query).queryParam("count", 200);

        Invocation.Builder invocationBuilder = callTarget.request();

        invocationBuilder.header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        invocationBuilder.header("Authorization", String.format("Bearer %s", token));

        Response response = invocationBuilder.get();
        String json = response.readEntity(String.class);

        return gson.fromJson(json, Statuses.class).getTweets();
    }

    private Client proxyHttpClient() {
        ClientConfig cc = new ClientConfig();
        cc.property(ClientProperties.PROXY_URI, PROXY_ADDRESS);
        cc.connectorProvider(new ApacheConnectorProvider());
        return ClientBuilder.newClient(cc);
    }
}
