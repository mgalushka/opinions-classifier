package com.maximgalushka.driller;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.io.IOException;

/**
 * Based on:
 * <a href='http://stackoverflow.com/a/1457173/2075157'>
 * http://stackoverflow.com/a/1457173/2075157
 * </a>
 *
 * @author Maxim Galushka
 */
public class DrillerImpl implements Driller {

  @Override
  public String resolve(String url) throws IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet httpget = new HttpGet(url);
    HttpContext context = new BasicHttpContext();
    HttpResponse response = httpClient.execute(httpget, context);
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      throw new IOException(response.getStatusLine().toString());
    }
    HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(
      HttpCoreContext.HTTP_REQUEST
    );
    HttpHost currentHost = (HttpHost) context.getAttribute(
      HttpCoreContext.HTTP_TARGET_HOST
    );
    return (currentReq.getURI().isAbsolute()) ?
      currentReq.getURI().toString() : (
      currentHost.toURI() + currentReq.getURI());
  }

  @Override
  public void asyncResolve(
    String url, DrillerCallback callback
  ) {
    throw new UnsupportedOperationException();
  }
}
