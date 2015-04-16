/**
 *
 */
package com.maximgalushka.classifier.clustering.lsh.simhash;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangcheng
 */
public final class BinaryWordSeg implements IWordSeg {

  @Override
  public List<String> tokens(String doc) {
    List<String> binaryWords = new LinkedList<>();
    for (int i = 0; i < doc.length() - 1; i += 1) {
      binaryWords.add(String.valueOf(doc.charAt(i)) + doc.charAt(i + 1));
    }
    return binaryWords;
  }

}
