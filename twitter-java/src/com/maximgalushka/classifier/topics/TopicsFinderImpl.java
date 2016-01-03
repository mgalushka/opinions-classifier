package com.maximgalushka.classifier.topics;

import com.google.common.collect.Lists;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * @author Maxim Galushka
 */
public class TopicsFinderImpl implements TopicsFinder {

  public static final Logger log = Logger.getLogger(TopicsFinderImpl.class);

  @Override
  public List<String> findTopics(List<Tweet> tweets, int number)
  throws Exception {
    /**
     * Process is pipeline consistent from next steps:
     * 1. Create temp dir for each request
     * 2. Extract all tweets to corresponding files to that dir
     * 3. Run seqdirectory
     * 4. Run seq2sparse
     * 5. Run lda
     * 6. Run LDAPrintTopics
     * 7. Cleanup everything
     */
    Path outTempDir = Files.createTempDirectory(
      String.valueOf(UUID.randomUUID())
    );

    // 2. Dump tweets ===========================================
    Path outTweetsDir = Paths.get(outTempDir.toString(), "tweets");
    Files.createDirectories(outTweetsDir);
    try {
      log.debug(
        String.format(
          "Extracting all tweets to temp dir: %s",
          outTweetsDir.toString()
        )
      );
      int counter = 0;
      for (Tweet t : tweets) {
        PrintWriter pw = new PrintWriter(
          Paths.get(
            outTweetsDir.toString(),
            String.format("%03d_tweet.txt", counter)
          ).normalize().toFile()
        );
        // TODO: normalize???
        pw.println(t.getText());
        counter++;
        pw.close();
      }

      // 3. seqdirectory ===========================================
      Path outputSeqDir = Paths.get(outTempDir.toString(), "seqfiles");
      log.debug(
        String.format(
          "Run mahout seqdirectory on: %s,\noutput: %s",
          outTweetsDir.toString(),
          outputSeqDir.toString()
        )
      );
      SequenceFilesFromDirectory.main(
        new String[]{
          "-c", "UTF-8",
          "-i", outTweetsDir.toString(),
          "-o", outputSeqDir.toString(),
        }
      );

      // 4. seq2sparse ===========================================
      Path seq2sparseDir = Paths.get(outTempDir.toString(), "seq2sparse");
      log.debug(
        String.format(
          "Run mahout seq2sparse on: %s,\noutput: %s",
          outputSeqDir.toString(),
          seq2sparseDir.toString()
        )
      );
      SparseVectorsFromSequenceFiles.main(
        new String[]{
          "-i", outputSeqDir.toString(),
          "-o", seq2sparseDir.toString(),
          "-ow",
        }
      );

    } finally {
      log.trace("Cleaning temp directory");
      // TODO: un-comment for production
      //FileUtils.deleteDirectory(outTempDir.toFile());
    }
    return Lists.newArrayList();
  }

}
