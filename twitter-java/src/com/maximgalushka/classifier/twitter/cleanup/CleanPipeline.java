package com.maximgalushka.classifier.twitter.cleanup;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public class CleanPipeline implements Cleaner {

  public static final Logger log = Logger.getLogger(CleanPipeline.class);

  private List<Cleaner> cleaners = new ArrayList<>();

  public List<Cleaner> getCleaners() {
    return cleaners;
  }

  public void setCleaners(List<Cleaner> cleaners) {
    this.cleaners = cleaners;
  }

  @Override
  public String clean(String input) {
    String intermediate = input;
    for (Cleaner cleaner : cleaners) {
      intermediate = cleaner.clean(intermediate);
    }
    log.trace(
      String.format(
        "Cleaned from [%s] to [%s]",
        input,
        intermediate
      )
    );
    return intermediate;
  }

  public void batchClean(List<Tweet> tweets) {
    for (Tweet tweet : tweets) {
      tweet.setText(clean(tweet.getText()));
    }
  }

  public static void main(String[] args) {
    ApplicationContext ac =
      new ClassPathXmlApplicationContext(
        "spring/classifier-services.xml"
      );
    CleanPipeline cleanPipeline = (CleanPipeline) ac.getBean(
      "tweets-cleaner-pipeline"
    );

    StorageService storage = (StorageService) ac.getBean(
      "storage"
    );

    // some small cluster from history...
    List<Tweet> tweets = storage.getTweetsForRun(
      storage.getActiveAccounts().get(0).getId(),
      29
    );
    log.debug(String.format("Found [%d] tweets, cleaning...", tweets.size()));
    cleanPipeline.batchClean(tweets);
    storage.saveTweetsCleanedBatch(tweets);

    System.exit(0);
  }
}
