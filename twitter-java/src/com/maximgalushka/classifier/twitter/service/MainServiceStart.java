package com.maximgalushka.classifier.twitter.service;

import com.google.gson.Gson;
import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.stream.TwitterStreamProcessor;
import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * TODO: 1 thread server, design is exceptionally bad
 *
 * @since 8/29/2014.
 */
public class MainServiceStart implements Container {

  public static final Logger log = Logger.getLogger(MainServiceStart.class);

  @SuppressWarnings("UnusedDeclaration")
  private static final long HOURS24 = 24 * 60 * 60 * 1000;

  @SuppressWarnings("UnusedDeclaration")
  private static final long HOURS6 = 6 * 60 * 60 * 1000;

  @SuppressWarnings("UnusedDeclaration")
  private static final long HOURS1 = 60 * 60 * 1000;

  private StorageService storage;
  private LocalSettings settings;
  private final Gson gson;

  public MainServiceStart() {
    this.gson = new Gson();
  }

  public void setStorage(StorageService storage) {
    this.storage = storage;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setSettings(LocalSettings settings) {
    this.settings = settings;
  }

  public void headers(Response response) {
    long time = System.currentTimeMillis();
    response.setValue("Content-Type", "application/json");
    response.setValue("Server", "Tweets Clustering Classifier");
    response.setDate("Date", time);
    response.setDate("Last-Modified", time);
    /** I leave it commented here as a reference for future
     This is fixed with proper apache server reverse proxy configuration
     <VirtualHost *:80>
     ProxyPreserveHost On
     ProxyRequests Off
     ServerName host
     ServerAlias host
     ProxyPass /api http://localhost:8090
     ProxyPassReverse /api http://localhost:8090
     </VirtualHost>
     */
    // response.setValue("Access-Control-Allow-Origin", "*");
  }

  public void handle(Request request, Response response) {
    try {
      headers(response);
      PrintStream body = response.getPrintStream();
      Clusters clusters = new Clusters();
      clusters.addClustersNoIndex(storage.findClusters(HOURS1));
      body.println(gson.toJson(clusters));
      body.close();
      log.debug("Response sent");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] list) throws Exception {
    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );

    ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor)
      ac.getBean("taskExecutor");
    MainServiceStart container = (MainServiceStart) ac.getBean("main");
    Server server = new ContainerServer(container);
    Connection connection = new SocketConnection(server);

    int port = Integer.valueOf(
      container.settings.value(LocalSettings.WEB_PORT)
    );
    SocketAddress address = new InetSocketAddress(port);
    connection.connect(address);
    log.debug(String.format("Server started on port [%d]", port));

    TwitterStreamProcessor processor = (TwitterStreamProcessor)
      ac.getBean("twitter-stream-processor");

    pool.execute(processor);
    log.debug("Twitter stream processor started");

    // TODO: add credentials to be able to shutdown server
    StopServiceHandler stopHandler = (StopServiceHandler)
      ac.getBean("stop-service");
    pool.execute(stopHandler);
    log.debug("Started application stop interface");
  }

}
