package com.maximgalushka.classifier.twitter.service;

import com.maximgalushka.classifier.storage.StorageService;
import com.maximgalushka.classifier.twitter.LocalSettings;
import com.maximgalushka.classifier.twitter.account.TwitterAccount;
import com.maximgalushka.classifier.twitter.client.TwitterStandardClient;
import com.maximgalushka.classifier.twitter.model.ScheduledTweet;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Maxim Galushka
 */
public class TweetPublishScheduler implements Runnable {
  public static final Logger log =
      Logger.getLogger(TweetPublishScheduler.class);

  private static final ScheduledExecutorService pool =
      Executors.newScheduledThreadPool(2);

  private boolean started = false;

  private StorageService storage;
  @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
  private LocalSettings settings;

  private TwitterStandardClient twitter;

  public TweetPublishScheduler() {
  }

  public void setStorage(StorageService storage) {
    this.storage = storage;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setSettings(LocalSettings settings) {
    this.settings = settings;
  }

  public void setTwitter(TwitterStandardClient twitter) {
    this.twitter = twitter;
  }

  // interval between tweets on same day (approx.)
  private static final int NEW_TWEETS_INTERVAL_HOURS = 3;

  // interval between re-tweets on same day (approx.)
  private static final int RETWEETS_INTERVAL_HOURS = 2;

  // working hours in UTC tomezone - should I post in US zone?
  private static final int[] WORKING_HOURS = {7, 22};

  @Override
  public void run() {
    List<TwitterAccount> accounts = this.storage.getActiveAccounts();
    for (TwitterAccount account : accounts) {
      scheduleForAccount(account.getId());
    }
  }

  /**
   * Gets unscheduled tweets from storage.
   * Schedule them based on caps.
   */
  public void scheduleForAccount(long accountId) {
    log.debug(
        String.format(
            "Picking scheduled but unpublished tweets for account %d,",
            accountId
        )
    );
    List<ScheduledTweet> unpublished = storage.getScheduledUnpublishedTweets(
        accountId
    );
    log.debug(
        String.format(
            "Scheduled but unpublished tweets for account [%d]:\n%s",
            accountId,
            unpublished.size() == 0 ? "[none]" : unpublished
        )
    );
    // schedule unpublished tweets only once at application startup and no more after that
    if (!started) {
      unpublished.forEach(this::schedule);
      started = true;
    }

    log.debug(
        String.format(
            "Getting unscheduled tweets for account [%d]",
            accountId
        )
    );
    List<ScheduledTweet> tweets = storage.getUnscheduledTweets(accountId);
    log.debug(
        String.format(
            "Unscheduled tweets for account [%d]:\n%s",
            accountId,
            tweets.size() == 0 ? "[none]" : tweets
        )
    );
    Date latestNew = storage.getLatestPublishedOrScheduledTimestamp(
        accountId,
        false
    );
    Date latestRT = storage.getLatestPublishedOrScheduledTimestamp(
        accountId,
        true
    );

    Calendar latestNewCalendar = fromDate(latestNew);
    Calendar latestRTCalendar = fromDate(latestRT);

    log.debug(
        String.format(
            "Latest new tweet timestamp for account [%d]: [%s]",
            accountId,
            SDF.format(latestNew)
        )
    );
    log.debug(
        String.format(
            "Latest re-tweet timestamp for account [%d]: [%s]",
            accountId,
            SDF.format(latestRT)
        )
    );

    for (ScheduledTweet tweet : tweets) {
      boolean retweet = tweet.isRetweet();
      int shift = minutesShift(retweet);
      Date scheduled;
      if (retweet) {
        latestRTCalendar.add(Calendar.MINUTE, shift);
        adjust(latestRTCalendar);
        scheduled = latestRTCalendar.getTime();
      } else {
        latestNewCalendar.add(Calendar.MINUTE, shift);
        adjust(latestNewCalendar);
        scheduled = latestNewCalendar.getTime();
      }
      storage.updateScheduled(tweet.getData().getId(), scheduled);
      tweet.setScheduled(scheduled);
      schedule(tweet);
      log.debug(
          String.format(
              "Scheduled for account [%d]: %s",
              accountId,
              tweet
          )
      );
    }
  }

  private static Calendar fromDate(Date dt) {
    Calendar c = Calendar.getInstance();
    c.setTime(dt);
    return c;
  }

  private static final SimpleDateFormat SDF =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static void adjust(Calendar init) {
    int h = init.get(Calendar.HOUR_OF_DAY);
    if (!(h >= WORKING_HOURS[0] && h <= WORKING_HOURS[1])) {
      Date before = init.getTime();
      init.add(Calendar.HOUR, 7);
      log.debug(
          String.format(
              "Date [%s] is not during working hours, adjusting to [%s]",
              SDF.format(before),
              SDF.format(init.getTime())
          )
      );
    }
  }

  private Random r = new Random(System.currentTimeMillis());

  private int minutesShift(boolean retweet) {
    int interval = retweet ?
        RETWEETS_INTERVAL_HOURS :
        NEW_TWEETS_INTERVAL_HOURS;
    int MIN = 60 * interval - 30;
    return r.nextInt(60) + MIN;
  }

  private void schedule(final ScheduledTweet tweet) {
    long current = System.currentTimeMillis();
    long scheduled = tweet.getScheduled().getTime();
    long delay = scheduled - current;
    if (delay < 0) {
      log.error(
          String.format(
              "Tweet %s is scheduled in the PAST. Ignoring.",
              tweet
          )
      );
    }
    pool.schedule(
        () -> {
          try {
            log.debug(
                String.format(
                    "Publishing to actual account: %s",
                    tweet
                )
            );
            boolean retweet = tweet.isRetweet();
            Status status = null;
            boolean success = true;
            try {
              TwitterAccount account = storage.getAccountById(
                  tweet.getData().getAccountId()
              );
              if (retweet) {
                status = twitter.retweet(account, tweet.getData().getId());
              } else {
                status = twitter.post(account, tweet.getData(), false, false);
              }
            } catch (TwitterException ex) {
              success = false;
              log.error(
                  "Tweet action failed.",
                  ex
              );
              storage.updatePublished(
                  tweet.getData().getId(),
                  -1,
                  false
              );
            }
            if (status != null) {
              long publishedId = status.getId();
              //noinspection ConstantConditions
              storage.updatePublished(
                  tweet.getData().getId(),
                  publishedId,
                  success
              );
            }
          } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
          }
        },
        delay, TimeUnit.MILLISECONDS
    );
  }

  public static void main(String[] args) {
    ApplicationContext ac =
        new ClassPathXmlApplicationContext(
            "spring/classifier-services.xml"
        );

    TweetPublishScheduler scheduler = (TweetPublishScheduler)
        ac.getBean("scheduler");

    pool.scheduleWithFixedDelay(scheduler, 0, 1, TimeUnit.HOURS);
    log.debug("Twitter scheduler started");
  }
}
