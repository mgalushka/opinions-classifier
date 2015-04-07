package com.maximgalushka.classifier.twitter.service;

import com.google.gson.Gson;
import com.maximgalushka.classifier.clustering.ClusteringPipeline;
import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.stream.TwitterStreamProcessor;
import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

  @SuppressWarnings("FieldCanBeLocal")
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
    response.setValue("Access-Control-Allow-Origin", "*");
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

    ScheduledExecutorService pool = Executors.newScheduledThreadPool(4);
    // TODO: disable web-site as we pivoted to publish to twitter only for now.
    /*
    MainServiceStart container = (MainServiceStart) ac.getBean("main");
    Server server = new ContainerServer(container);
    Connection connection = new SocketConnection(server);

    int port = Integer.valueOf(
      container.settings.value(LocalSettings.WEB_PORT)
    );
    SocketAddress address = new InetSocketAddress(port);
    connection.connect(address);
    log.debug(String.format("Server started on port [%d]", port));
    */

    TwitterStreamProcessor processor = (TwitterStreamProcessor)
      ac.getBean("twitter-stream-processor");

    pool.execute(processor);
    log.debug("Twitter stream processor started");

    final ClusteringPipeline pipeline = (ClusteringPipeline) ac.getBean(
      "twitter-classifier-pipeline"
    );
    pool.scheduleAtFixedRate(
      pipeline::clusterFromStorage,
      0, 30, TimeUnit.MINUTES
    );

    // TODO: add credentials to be able to shutdown server
    StopServiceHandler stopHandler = (StopServiceHandler)
      ac.getBean("stop-service");
    pool.execute(stopHandler);
    log.debug("Started application stop interface");
  }

}
