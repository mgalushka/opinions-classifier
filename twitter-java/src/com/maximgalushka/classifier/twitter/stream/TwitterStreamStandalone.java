package com.maximgalushka.classifier.twitter.stream;

import com.maximgalushka.classifier.twitter.client.PollingTwitterSearchClient;
import com.maximgalushka.classifier.twitter.classify.carrot
  .ClusteringTweetsListAlgorithm;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.ArrayDeque;
import java.util.concurrent.*;

import static com.maximgalushka.classifier.twitter.classify.Tools.*;

/**
 * @since 8/11/2014.
 */
public class TwitterStreamStandalone {

  public static final Logger log = Logger.getLogger(
    TwitterStreamStandalone
      .class
  );
  private static boolean NO_CLASSIFICATION = false;

  @SuppressWarnings("InfiniteLoopStatement")
  public static void main(String[] args)
  throws FileNotFoundException, UnsupportedEncodingException {
    if (args.length == 0) {
      return;
    }
    if (args.length > 1) {
      String second = args[1];
      if (second != null && !"".equals(second.trim())) {
        if ("--no-classification".equals(second.trim())) {
          System.out.println("Running stream without classification");
          NO_CLASSIFICATION = true;
        }
      }
    }

    //System.setProperty("http.proxyHost", "localhost");
    //System.setProperty("http.proxyPort", "4545");

    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );

    ClusteringTweetsListAlgorithm clustering =
      (ClusteringTweetsListAlgorithm) ac.getBean("lingo-clustering-algorithm");
    PollingTwitterSearchClient client = (PollingTwitterSearchClient) ac.getBean(
      "twitter-stream-client"
    );

    final PrintWriter pw = new PrintWriter(args[0]);

    /*
    BlockingQueue<String> q = new ArrayBlockingQueue<>(100);
    final Client hosebirdClient = client.stream(q);
    hosebirdClient.connect();
    */

    BlockingQueue<Tweet> q = new ArrayBlockingQueue<>(100);
    client.stream("rembrandt", q);

    Runtime.getRuntime().addShutdownHook(
      new Thread() {
        @Override
        public void run() {
          System.out.println("Shutdown");
          pw.close();
        }
      }
    );
    int BATCH_SIZE = 1000;
    // TODO: experiment to find better ratio
    int STEP = BATCH_SIZE / 10;

    ArrayDeque<Tweet> batch = new ArrayDeque<>();
    while (true) {
      try {
        Tweet tweet = q.take();
        //Tweet tweet = gson.fromJson(json, Tweet.class);
        if (!NO_CLASSIFICATION) {
          try {
            // we need to collect full batch of elements and then classify
            // the whole batch
            if (batch.size() == (BATCH_SIZE + STEP)) {
              clustering.classify(slice(batch, BATCH_SIZE), new Clusters());

              // remove first STEP elements from start
              cleanFromStart(batch, STEP);
            } else {
              batch.addLast(tweet);
            }
          } catch (IOException e) {
            log.error(e);
            e.printStackTrace();
          }
        } else {
          String tweetSingleLine = tweet.getText().trim().replaceAll(
            "\r?\n",
            " "
          );
          log.debug(String.format("[%d] %s", tweet.getId(), tweetSingleLine));
        }
      } catch (InterruptedException e) {
        log.error(e);
        e.printStackTrace();
      }
    }
  }
}
