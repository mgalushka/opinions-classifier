package com.maximgalushka.classifier.clustering.legacy;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maxim Galushka
 */
@Immutable
public final class TfIdfSparseVector implements SparseVector<Double> {

    private final Map<Integer, Double> vector;

    public TfIdfSparseVector(Map<Integer, Double> vector) {
        this.vector = new HashMap<Integer, Double>(vector);
    }

    @Override
    public Map<Integer, Double> getVector() {
        return Collections.unmodifiableMap(vector);
    }
}
