package com.maximgalushka.classifier.clustring.model;

import com.maximgalushka.classifier.twitter.classify.TextCleanup;
import org.tartarus.snowball.ext.EnglishStemmer;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Vector used to store count of each word in text
 *
 * @author Maxim Galushka
 */
@NotThreadSafe
public final class WordCountSparseVector implements SparseVector<Long> {

    private Map<Integer, Long> vector = new HashMap<Integer, Long>();

    public WordCountSparseVector() {
    }

    public WordCountSparseVector(String dirty) {
        addText(dirty);
    }

    @Override
    public Map<Integer, Long> getVector() {
        return Collections.unmodifiableMap(vector);
    }

    public synchronized void addText(String stemmedTestAdded) {
        EnglishStemmer stemmer = new EnglishStemmer();
        String[] tokens = cleanSplit(stemmedTestAdded);
        for (String token : tokens) {
            stemmer.setCurrent(token);
            if (stemmer.stem()) {
                String stemmed = stemmer.getCurrent();
                int hash = stemmed.hashCode();
                Long value = vector.get(hash);
                if (value == null) {
                    vector.put(hash, 1L);
                } else {
                    vector.put(hash, value + 1);
                }
            } else {
                throw new RuntimeException(
                        String.format("Cannot stem input word: [%s]", token));
            }
        }
    }

    private static final String REMOVE_REGEXP =
            ",|\\.|:|\\\\|/|\"|'|#|$|&|^|@|!|~|`|-|\\]|\\[|%|\\*|\\(|\\)|\\+|\\?|>|<|;|=";

    private static String[] cleanSplit(String input) {
        if (input == null) return null;
        String tweetCleaned = TextCleanup.reformatMessage(input);
        return tweetCleaned.replaceAll(REMOVE_REGEXP, "")
                .trim().split("\\s+");
    }

}
