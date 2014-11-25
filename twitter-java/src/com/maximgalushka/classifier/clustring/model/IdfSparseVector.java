package com.maximgalushka.classifier.clustring.model;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maxim Galushka
 */
@NotThreadSafe
public final class IdfSparseVector {

    private Map<Integer, Long> vector = new HashMap<Integer, Long>();

    public IdfSparseVector() {
    }

    public IdfSparseVector(String stemmedText) {
        addText(stemmedText);
    }

    public Map<Integer, Long> getVector(){
        return Collections.unmodifiableMap(vector);
    }

    public synchronized void addText(String stemmedTestAdded){
        String[] tokens = stemmedTestAdded.split("\\s+");
        for (String token : tokens) {
            int hash = token.hashCode();
            Long value = vector.get(hash);
            if (value == null) {
                vector.put(hash, 1L);
            } else {
                vector.put(hash, value + 1);
            }
        }
    }

}
