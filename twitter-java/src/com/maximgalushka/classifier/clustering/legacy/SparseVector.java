package com.maximgalushka.classifier.clustering.legacy;

import java.util.Map;

/**
 * @author Maxim Galushka
 */
public interface SparseVector <T>{

    /**
     * @return map correspondent to hash - value
     */
    public Map<Integer, T> getVector();
}
