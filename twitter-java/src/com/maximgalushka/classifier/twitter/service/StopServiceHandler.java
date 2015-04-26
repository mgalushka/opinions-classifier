package com.maximgalushka.classifier.twitter.service;

import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.stream.TwitterStreamProcessor;
import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @since 9/11/2014.
 */
@SuppressWarnings("UnusedDeclaration")
public class StopServiceHandler implements Runnable {

  public static final Logger log = Logger.getLogger(StopServiceHandler.class);
  private LocalSettings settings;

  // TODO: dirty hack - figure out how spring can initialize thread pool in spring via static
  // factory
  private ExecutorService pool = Executors.newFixedThreadPool(1);

  private TwitterStreamProcessor processor;

  public StopServiceHandler() {
  }

  @Override
  public void run() {
    final int port = Integer.valueOf(
      settings.value(LocalSettings.SHUTDOWN_PORT)
    );

    //start kill listener for self
    pool.execute(
      () -> {
        try {
          ServerSocket serverSocket = new ServerSocket(port);
          log.debug(
            String.format(
              "Started shutdown service on port [%d]",
              port
            )
          );
          serverSocket.accept();
          long TIMEOUT = 1000;
          log.warn(
            String.format(
              "Received shutdown signal, " +
                "stopping server with timeout [%d] millis",
              TIMEOUT
            )
          );
          processor.sendStopSignal();
          Thread.sleep(TIMEOUT);

          serverSocket.close();
        } catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        System.exit(0);
      }
    );
  }

  public LocalSettings getSettings() {
    return settings;
  }

  public void setSettings(LocalSettings settings) {
    this.settings = settings;
  }

  public TwitterStreamProcessor getProcessor() {
    return processor;
  }

  public void setProcessor(TwitterStreamProcessor processor) {
    this.processor = processor;
  }
}
