package com.maximgalushka.classifier.twitter;

import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.model.Statuses;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.maximgalushka.classifier.twitter.model.TwitterOAuthToken;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Maxim Galushka
 */
public class TwitterClient {

    private Gson gson;

    public TwitterClient() {
        this.gson = new Gson();
    }

    /**
     * @return access token
     */
    public String oauth() {
        Client client = ClientBuilder.newClient(new ClientConfig());
        WebTarget target = client.target("https://api.twitter.com/oauth2/token");
        WebTarget callTarget = target.queryParam("grant_type", "client_credentials");

        Invocation.Builder invocationBuilder = callTarget.request();

        invocationBuilder.header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        String secret = String.format("%s:%s", TwitterSettings.CONSUMER_KEY, TwitterSettings.CONSUMER_SECRET);
        String encoded = new String(Base64.encodeBase64(secret.getBytes()));
        invocationBuilder.header("Authorization", String.format("Basic %s", encoded));

        Response response = invocationBuilder.post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));
        String json = response.readEntity(String.class);

        return gson.fromJson(json, TwitterOAuthToken.class).getAccessToken();
    }

    public List<Tweet> search(String token, String query) {
        Client client = ClientBuilder.newClient(new ClientConfig());
        WebTarget search = client.target("https://api.twitter.com/1.1/search/tweets.json");
        WebTarget callTarget = search.queryParam("q", query).queryParam("count", 200);

        Invocation.Builder invocationBuilder = callTarget.request();

        invocationBuilder.header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        invocationBuilder.header("Authorization", String.format("Bearer %s", token));

        Response response = invocationBuilder.get();
        String json = response.readEntity(String.class);

        return gson.fromJson(json, Statuses.class).getTweets();
    }
}
