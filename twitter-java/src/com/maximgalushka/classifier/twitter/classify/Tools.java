package com.maximgalushka.classifier.twitter.classify;

import java.util.*;

/**
 * @since 8/29/2014.
 */
public class Tools {

    /**
     * Removes first N elements from deque
     *
     * @param deque deque
     * @param N     number of elements to remove
     */
    public static void cleanFromStart(ArrayDeque<?> deque, int N) {
        while (N-- > 0 && !deque.isEmpty()) {
            deque.removeFirst();
        }
    }

    /**
     * Converts first N elements from deque to list
     *
     * @param deque deque
     * @param N     number of elements to slice
     * @return sliced list
     */
    public static <T> ArrayList<T> slice(ArrayDeque<T> deque, int N) {
        ArrayList<T> result = new ArrayList<T>(N);
        Iterator<T> it = deque.iterator();
        while (it.hasNext() && N-- > 0) {
            result.add(it.next());
        }
        return result;
    }

    public static <T> ArrayList<T> slice(ArrayDeque<T> deque, int from, int to) {
        if (to < from) throw new IllegalArgumentException("From index cannot be greater then to index");
        ArrayList<T> result = new ArrayList<T>(to - from);
        Iterator<T> it = deque.iterator();
        int counter = to - from;
        while (it.hasNext()) {
            if (from-- > 0) continue;
            result.add(it.next());
            counter--;
            if (counter <= 0) break;
        }
        return result;
    }
}
