package com.maximgalushka.classifier.clustering.lsh.simhash;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.maximgalushka.classifier.clustering.graphs.ConnectedComponents;
import com.maximgalushka.classifier.clustering.graphs.Graph;
import com.maximgalushka.classifier.twitter.model.Tweet;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangcheng
 */
public class SimhashDuplicates {

  public static final Logger log = Logger.getLogger(SimhashDuplicates.class);

  public Graph buildTweetsGraph(List<Tweet> tweets) {
    List<String> docs = new ArrayList<>(tweets.size());
    for (Tweet t : tweets) {
      docs.add(t.getText());
    }
    return buildGraph(docs);
  }

  public Graph buildGraph(List<String> docs) {

    // Creates SimHash object.
    Simhash simHash = new Simhash(new BinaryWordSeg());

    // DocHashes is a list that will contain all of the calculated hashes.
    ArrayList<Long> docHashes = Lists.newArrayList();

    // Maps 12-bit key with the documents matching the partial hash
    Map<BitSet, HashSet<Integer>> hashIndex = Maps.newHashMap();

    // this is effectively unique id of the document
    int idx = 0;

    log.debug("Start to build simhash index...");
    for (String doc : docs) {
      // Calculate the document hash.
      long docHash = simHash.simhash64(doc);
      log.trace("Document=[" + doc + "] Hash=[" + docHash + "]");

      // Store the document hash in a list.
      docHashes.add(docHash);

      BitSet key = new BitSet(12);

      int step = 0;

      for (int i = 0; i < 64; ++i) {
        key.set(step, ((docHash >> i) & 1) == 1);
        if (step++ == 12) {
          /*
           * a) Separates the hash in 12-bit keys.
           * b) This value will be a key in hashIndex.
           * c) hashIndex will contain sets of
           *    documents matching each key (12-bits).
           */
          if (hashIndex.containsKey(key)) {
            hashIndex.get(key).add(idx);
          } else {
            HashSet<Integer> vector = new HashSet<>();
            vector.add(idx);
            hashIndex.put(key, vector);
          }
          step = 0;
          key = new BitSet(12); // reset key holder.
        }
      }
      ++idx;
    }
    log.debug("Simhash index has been built.");

    idx = 0;
    BitSet bits = new BitSet(docs.size());

    // graph to calculate clusters at the end as connected components in it
    Graph graph = new Graph(docs.size());

    for (String doc : docs) {
      // For each document.

      // don't compare document with itself
      if (bits.get(idx)) {
        ++idx;
        continue;
      }

      // Calculates document hash.
      long docHash = simHash.simhash64(doc);
      BitSet key = new BitSet(12);

      int step = 0;
      HashSet<Integer> docSimilarCandidates = Sets.newHashSet();
      for (int i = 0; i < 64; ++i) {
        key.set(step, ((docHash >> i) & 1) == 1);

        if (step++ == 12) {
          /*
           * a) Separates the hash in 12-bit keys.
           * b) This value will be a key in hashIndex.
           * c) hashIndex will contain sets of
           *   documents matching each key (12-bits).
           */
          if (hashIndex.containsKey(key)) {
            docSimilarCandidates.addAll(hashIndex.get(key));
          }
          step = 0;
          key = new BitSet(12);
        }
      }
      List<Integer> similarDocs = Lists.newLinkedList();
      Map<Integer, Integer> docDistances = Maps.newHashMap();

      // go through each candidate and make decision if this is actual
      // match by using other metric - like Hamming distance in this example
      for (Integer i : docSimilarCandidates) {
        int dist = simHash.hammingDistance(docHash, docHashes.get(i));
        // TODO: setup the threshold
        if (dist <= 10) {
          similarDocs.add(i);
          bits.set(idx);
          docDistances.put(i, dist);
        }
      }
      if (!similarDocs.isEmpty()) {
        /*
        Files.append(
          "Documents similar as [" + doc + "]:\n",
          output,
          Charsets.UTF_8
        );
        */
        for (int i : similarDocs) {
          if (i == idx) {
            continue;
          }
          /*
          Files.append(
            "[" + docs.get(i) + "]\tDistance=[" + docDistances.get(i) + "]\n",
            output,
            Charsets.UTF_8
          );
          */
          graph.addEdge(idx, i);
        }
        //Files.append("End\n", output, Charsets.UTF_8);
      }
      bits.set(idx);
      ++idx;
    }

    // TODO: idea on how to speed up checking if new best representatives are
    // TODO same tweets that were published previously:
    // let first N tweets are existing tweets (published already or rejected
    // already)
    // build a graph and after it go through connected components starting from
    // 1..N for each already twitted/rejected tweet.
    // and mark all tweets in this CC as duplicates with corresponding reason
    // to spidify the process - if CC was marked - don't repeat it
    // if there are self-duplicates across 1..N by themselves.
    // 1..N - to choose for latest 30 days to avoid huge number of messages.
    return graph;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: inputfile outputfile");
      return;
    }
    long start = System.nanoTime();
    List<String> docs = readDocs(args);
    File output = new File(args[1]);

    SimhashDuplicates duplicates = new SimhashDuplicates();
    List<List<Integer>> cc = ConnectedComponents.getComponents(
      duplicates.buildGraph(docs)
    );

    int clusterId = 0;

    Files.append(
      String.format(
        "=========== TOTAL: %d ============\n\n\n",
        cc.size()
      ),
      output,
      Charsets.UTF_8
    );

    for (List<Integer> component : cc) {
      Files.append(
        String.format(
          "=========== %d (%d) ============\n",
          clusterId++,
          component.size()
        ),
        output,
        Charsets.UTF_8
      );
      for (int i : component) {
        Files.append(
          docs.get(i) + "\n",
          output,
          Charsets.UTF_8
        );
      }
      Files.append(
        "\n",
        output,
        Charsets.UTF_8
      );
    }

    System.out.println(
      "Elapsed time: " + TimeUnit.NANOSECONDS.toMillis(
        System.nanoTime() - start
      )
    );
  }

  private static List<String> readDocs(String[] args) throws IOException {
    return Files.readLines(new File(args[0]), Charsets.UTF_8);
  }
}