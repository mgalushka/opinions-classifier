package com.maximgalushka.classifier.clustering.graphs;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Maxim Galushka
 */
public class Bag<Item> implements Iterable<Item> {
  private int N;               // number of elements in bag
  private Node<Item> first;    // beginning of bag

  // helper linked list class
  private static class Node<Item> {
    private Item item;
    private Node<Item> next;
  }

  /**
   * Initializes an empty bag.
   */
  public Bag() {
    first = null;
    N = 0;
  }

  /**
   * Is this bag empty?
   *
   * @return true if this bag is empty; false otherwise
   */
  public boolean isEmpty() {
    return first == null;
  }

  /**
   * Returns the number of items in this bag.
   *
   * @return the number of items in this bag
   */
  public int size() {
    return N;
  }

  /**
   * Adds the item to this bag.
   *
   * @param item the item to add to this bag
   */
  public void add(Item item) {
    Node<Item> oldfirst = first;
    first = new Node<>();
    first.item = item;
    first.next = oldfirst;
    N++;
  }


  /**
   * Returns an iterator that iterates over the items in the bag in arbitrary
   * order.
   *
   * @return an iterator that iterates over the items in the bag in arbitrary
   * order
   */
  public Iterator<Item> iterator() {
    return new ListIterator<>(first);
  }

  // an iterator, doesn't implement remove() since it's optional
  private class ListIterator<I> implements Iterator<I> {
    private Node<I> current;

    public ListIterator(Node<I> first) {
      current = first;
    }

    public boolean hasNext() {
      return current != null;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public I next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      I item = current.item;
      current = current.next;
      return item;
    }
  }

}


