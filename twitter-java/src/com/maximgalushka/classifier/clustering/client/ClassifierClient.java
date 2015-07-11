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
public class ClassifierClient {

  public static final Logger log = Logger.getLogger(ClassifierClient.class);

  private static final String CLASSIFIER_API =
    "http://warua.org:8077/classify?text=%s";

  public String getLabel(String tweet) {
    try {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(
        String.format(
          CLASSIFIER_API,
          URLEncoder.encode(tweet.replaceAll("\\p{C}", ""), "UTF-8")
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
    ClassifierClient client = new ClassifierClient();
    String l1 = client.getLabel("machine learning");
    log.debug(
      String.format(
        "Classified: [%s]",
        l1
      )
    );
  }
}
