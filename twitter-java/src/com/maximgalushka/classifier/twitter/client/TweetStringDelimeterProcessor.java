package com.maximgalushka.classifier.twitter.client;

import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.model.Tweet;
import com.twitter.hbc.common.DelimitedStreamReader;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.processor.AbstractProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

/**
 * Repeats code of StringDelimeterProcessor and just parses each message as a
 * tweet object from json.
 *
 * @author Maxim Galushka
 */
public class TweetStringDelimeterProcessor extends AbstractProcessor<Tweet> {

  private final static Logger logger = LoggerFactory.getLogger(
    TweetStringDelimeterProcessor.class
  );
  private final static int DEFAULT_BUFFER_SIZE = 50000;
  private final static int MAX_ALLOWABLE_BUFFER_SIZE = 500000;
  private final static String EMPTY_LINE = "";

  private DelimitedStreamReader reader;
  private Gson gson = new Gson();
  long twitterAccountId;

  public TweetStringDelimeterProcessor(
    long twitterAccountId,
    BlockingQueue<Tweet> queue
  ) {
    super(queue);
    this.twitterAccountId = twitterAccountId;
  }

  @Override
  public void setup(InputStream input) {
    reader = new DelimitedStreamReader(
      input,
      Constants.DEFAULT_CHARSET,
      DEFAULT_BUFFER_SIZE
    );
  }

  @Override
  @Nullable
  protected Tweet processNextMessage() throws IOException {
    int delimitedCount = -1;
    int retries = 0;
    while (delimitedCount < 0 && retries < 3) {
      String line = reader.readLine();
      if (line == null) {
        throw new IOException("Unable to read new line from stream");
      } else if (line.equals(EMPTY_LINE)) {
        return null;
      }

      try {
        delimitedCount = Integer.parseInt(line);
      } catch (NumberFormatException n) {
        // resilience against the occasional malformed message
        logger.warn("Error parsing delimited length", n);
      }
      retries += 1;
    }

    if (delimitedCount < 0) {
      throw new RuntimeException("Unable to process delimited length");
    }

    if (delimitedCount > MAX_ALLOWABLE_BUFFER_SIZE) {
      // this is to protect us from nastiness
      throw new IOException("Unreasonable message size " + delimitedCount);
    }
    String message = reader.read(delimitedCount);
    Tweet tweet = gson.fromJson(message, Tweet.class);

    tweet.setAccountId(twitterAccountId);
    return tweet;
  }
}
