package com.maximgalushka.classifier.twitter.stream;

import com.maximgalushka.classifier.twitter.TwitterStandardClient;
import com.maximgalushka.classifier.twitter.model.Statuses;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;

/**
 * We need this class to poll twitter with search requests
 * for different keywords and publish responses to blocking queue
 * to emulate publish/subscribe API.
 * <p/>
 * Why we added this - if we need to monitor a few topics simultaneously -
 * we cannot use twitter stream API as only 1 streaming process is allowed
 * to be run at a time.
 * <p/>
 * To overcome this - proposed interface allows to poll twitter
 * (within its search query limitations 450 queries per 15 min)
 * And post to blocking queue for processing which is compatible to existing
 * stream API interface.
 * <p/>
 *
 * @author Maxim Galushka
 * @see <a href="https://dev.twitter.com/rest/public/rate-limiting">
 * Twitter search query API limits</a>
 */
@SuppressWarnings("UnusedDeclaration")
public class StreamTwitterSearchWrapper {
  public static final Logger log = Logger.getLogger
    (StreamTwitterSearchWrapper.class);

  private final ScheduledExecutorService executor;

  // queue for pending queries to twitter
  private final BlockingDeque<String> pendingQueries;

  // max id which we need to query since_id with next query
  // to avoid duplicate tweets in results
  private final ConcurrentMap<String, Long> sinceTweetIds;

  // queues where we are storing results for queries
  // we can keep running a few queries simultaneously for different
  // search terms
  // Search term -> output blocking queue
  private final ConcurrentMap<String, BlockingQueue<Tweet>>
    results = new ConcurrentHashMap<>();

  private TwitterStandardClient twitterClient;

  public StreamTwitterSearchWrapper() {
    this.executor = Executors.newSingleThreadScheduledExecutor();
    this.pendingQueries = new LinkedBlockingDeque<>();
    this.sinceTweetIds = new ConcurrentHashMap<>();
  }

  @PostConstruct
  private void init() {
    start();
  }

  /**
   * Starts streaming queries to the blocking queue for specific term
   *
   * @param term   search term to poll twitter for updates
   * @param output blocking queue where service will send tweets
   */
  public void stream(String term, BlockingQueue<Tweet> output) {
    results.put(term, output);
    pendingQueries.add(term);
  }

  /**
   * Start this only once!
   */
  private synchronized void start() {
    // 450 search queries per 15 min == 1 query per 2 sec
    // thus - the scheduling on this method here
    executor.scheduleWithFixedDelay(
      () -> {
        log.trace(
          String.format(
            "Starting scheduled execution."
          )
        );
        try {
          String query = pendingQueries.takeFirst();
          String token = this.getTwitterClient().oauth();
          final long sinceId = sinceTweetIds.getOrDefault(query, 0L);
          Statuses statuses = this.getTwitterClient().search(
            token,
            query,
            sinceId
          );

          BlockingQueue<Tweet> result = results.get(query);
          if (result == null) {
            String error = String.format(
              "Result queue is not found. Where should I post results? " +
                "Exit now."
            );
            log.error(error);
            return;
          }
          List<Tweet> tweets = statuses.getTweets();
          log.trace(
            String.format(
              "Filtered new [%d] results for query: [%s]",
              tweets.size(),
              query
            )
          );
          result.addAll(tweets);
          sinceTweetIds.put(query, statuses.getMetadata().getMaxId());
          // return back to pending queue
          pendingQueries.addLast(query);
        } catch (InterruptedException interrupted) {
          interrupted.printStackTrace();
        }
      }, 0, 2, TimeUnit.SECONDS
    );
  }

  public TwitterStandardClient getTwitterClient() {
    return twitterClient;
  }

  public void setTwitterClient(TwitterStandardClient twitter) {
    this.twitterClient = twitter;
  }
}
