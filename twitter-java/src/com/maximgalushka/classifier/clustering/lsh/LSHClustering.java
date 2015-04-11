package com.maximgalushka.classifier.clustering.lsh;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * @author samuelg@colbenson.es
 */
public class LshClustering {

  private static final int P = 2;
  private static final int Q = 10;

  private static final int SEED = 8123012;

  //the seeds must not to be random. i need to be consistent in every run
  private static final int[][] SEEDS = {
    {-1748155493, 942440870, 1959625380, 1195150829, 1901157259,
      -1371459491, -1910007019, -774029598, 118890350, -900846115
    },
    {721054953, -356882150, -639484809, -431383328, -1384478884,
      -1966006285, 985420514, 267099577, -1655869404, -1225828373
    }
  };

  private final static String MAX_STR_32_VALUE = buildMaxString(32);

  private HashFunction[][] functionsTable = new HashFunction[P][Q];
  private HashFunction bucketFuncton = Hashing.murmur3_128(SEED);

  public LshClustering() {
    for (int i = 0; i < P; i++) {
      for (int j = 0; j < Q; j++) {
        //is it threadsafe? maybe it can be static ...
        functionsTable[i][j] = Hashing.murmur3_128(SEEDS[i][j]);
      }
    }
  }

  public Set<String> proccessClusterIds(Set<String> set) {
    Set<String> buckets = new HashSet<>(Q);

    //looooooooping madness!
    for (int i = 0; i < Q; i++) {
      StringBuilder sb = new StringBuilder(32 * P);

      for (int j = 0; j < P; j++) {

        String minhash = MAX_STR_32_VALUE;
        for (String str : set) {
          String hash = functionsTable[j][i].hashString(
            str, Charset.forName(
              "UTF-8"
            )
          ).toString();
          if (minhash.compareTo(hash) > 0) {
            minhash = hash;
          }//eoif
        }//eofor

        sb.append(minhash);
      }

      buckets.add(
        bucketFuncton.hashString(
          sb.toString(),
          Charset.forName("UTF-8")
        ).toString()
      );
    }

    return buckets;
  }

  private static String buildMaxString(int size) {
    StringBuilder sb = new StringBuilder(size);

    for (int i = 0; i < size; i++) {
      sb.append('\uFFFF');
    }

    return sb.toString();
  }

}
