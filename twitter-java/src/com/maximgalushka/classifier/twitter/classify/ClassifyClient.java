package com.maximgalushka.classifier.twitter.classify;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;

/**
 * @since 8/12/2014.
 */
public class ClassifyClient {

    public Classification classify(String tweet) throws UnsupportedEncodingException {
        Client client = ClientBuilder.newClient();
        WebTarget search = client.target("http://localhost:8080/classify");
        WebTarget callTarget = search.queryParam("tweet", tweet);

        Invocation.Builder invocationBuilder = callTarget.request();

        Response response = invocationBuilder.get();
        String html = response.readEntity(String.class);
        if (html == null || "".equals(html.trim())) return Classification.NEUTRAL;
        return Classification.fromKey(html.trim().toLowerCase().substring(0, 1));
    }
}
