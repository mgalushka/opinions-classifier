package com.maximgalushka.classifier.clustering.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Maxim Galushka
 */
public class TextExtractorClient {
  public static final Logger log = Logger.getLogger(ClassifierClient.class);

  private static final String EXTRACTOR_API =
    "http://warua.org:8080/retrieve?text=%s";

  public String getArticle(String url) {
    try {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(
        String.format(
          EXTRACTOR_API,
          URLEncoder.encode(url, "UTF-8")
        )
      );
      CloseableHttpResponse response = httpclient.execute(httpGet);
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        HttpEntity entity = response.getEntity();
        return IOUtils.toString(entity.getContent(), "UTF-8");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) {
    String url = "http://techcrunch" +
      ".com/2015/07/29/vector-plans-developer-platform-for-its-affordable" +
      "-luxury-smart-watch/";
    TextExtractorClient client = new TextExtractorClient();

    log.debug(String.format("Extracting URL: %s", url));
    String article = client.getArticle(url);
    log.debug(String.format("Extracted: \n%s", article));
    System.exit(0);
  }
}
