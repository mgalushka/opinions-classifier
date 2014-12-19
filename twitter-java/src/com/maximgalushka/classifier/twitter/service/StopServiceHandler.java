package com.maximgalushka.classifier.twitter.service;

import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.stream.TwitterStreamProcessor;
import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

/**
 * @since 9/11/2014.
 */
@SuppressWarnings("UnusedDeclaration")
public class StopServiceHandler implements Runnable {

  public static final Logger log = Logger.getLogger(StopServiceHandler.class);
  private LocalSettings settings;
  private ExecutorService pool;
  private TwitterStreamProcessor processor;

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

          processor.sendStopSignal();
          Thread.sleep(1000);

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

  public void setPool(ExecutorService pool) {
    this.pool = pool;
  }

  public ExecutorService getPool() {
    return pool;
  }

  public TwitterStreamProcessor getProcessor() {
    return processor;
  }

  public void setProcessor(TwitterStreamProcessor processor) {
    this.processor = processor;
  }
}
