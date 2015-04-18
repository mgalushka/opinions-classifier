package com.maximgalushka.classifier.clustering.graphs;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Maxim Galushka
 */
@SuppressWarnings("UnusedDeclaration")
public class Stack<Item> implements Iterable<Item> {
  private int N;                // size of the stack
  private Node<Item> first;     // top of stack

  // helper linked list class
  private static class Node<Item> {
    private Item item;
    private Node<Item> next;
  }

  /**
   * Initializes an empty stack.
   */
  public Stack() {
    first = null;
    N = 0;
  }

  /**
   * Is this stack empty?
   *
   * @return true if this stack is empty; false otherwise
   */
  public boolean isEmpty() {
    return first == null;
  }

  /**
   * Returns the number of items in the stack.
   *
   * @return the number of items in the stack
   */
  public int size() {
    return N;
  }

  /**
   * Adds the item to this stack.
   *
   * @param item the item to add
   */
  public void push(Item item) {
    Node<Item> oldfirst = first;
    first = new Node<>();
    first.item = item;
    first.next = oldfirst;
    N++;
  }

  /**
   * Removes and returns the item most recently added to this stack.
   *
   * @return the item most recently added
   * @throws java.util.NoSuchElementException if this stack is empty
   */
  public Item pop() {
    if (isEmpty()) {
      throw new NoSuchElementException("Stack underflow");
    }
    Item item = first.item;        // save item to return
    first = first.next;            // delete first node
    N--;
    return item;                   // return the saved item
  }


  /**
   * Returns (but does not remove) the item most recently added to this stack.
   *
   * @return the item most recently added to this stack
   * @throws java.util.NoSuchElementException if this stack is empty
   */
  public Item peek() {
    if (isEmpty()) {
      throw new NoSuchElementException("Stack underflow");
    }
    return first.item;
  }

  /**
   * Returns a string representation of this stack.
   *
   * @return the sequence of items in the stack in LIFO order, separated by
   * spaces
   */
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (Item item : this)
      s.append(item).append(" ");
    return s.toString();
  }


  /**
   * Returns an iterator to this stack that iterates through the items in
   * LIFO order.
   *
   * @return an iterator to this stack that iterates through the items in
   * LIFO order.
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
