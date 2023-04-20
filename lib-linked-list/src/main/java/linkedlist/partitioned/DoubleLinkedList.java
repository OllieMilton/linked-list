package linkedlist.partitioned;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A {@link List} implementation of a doubly linked list that also provides random access to it's contained elements.
 *
 * This implementation does not make use of indexing and therefore any method that attempts to use and index on the list interface throws {@link UnsupportedOperationException}
 *
 * @param <E> the generic type of the contained elements.
 */
public class DoubleLinkedList<E> extends AbstractSequentialList<E> {

  static class Node<E> {
    Node<E> prev;
    E item;
    Node<E> next;
    List<E> parent;

    Node(List<E> parent, E item, Node<E> prev) {
      this.parent = parent;
      this.item = item;
      this.prev = prev;
    }
  }

  private final Map<E, Node<E>> randomAccessMap;
  private Node<E> head;
  private Node<E> tail;
  private int size = 0;

  public DoubleLinkedList(Map<E, Node<E>> randomAccessMap) {
    this.randomAccessMap = randomAccessMap;
  }

  @Override
  public boolean add(E e) {
    if (head == null) {
      head = new Node<>(this, e, null);
      tail = head;
    } else {
      Node<E> after = tail;
      tail = new Node<>(this, e, tail);
      if (after != null) {
        after.next = tail;
      }
      tail.prev = after;
    }
    size ++;
    modCount ++;
    randomAccessMap.put(e, tail);
    return true;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return head == null;
  }

  @Override
  public boolean contains(Object o) {
    Node<E> node = randomAccessMap.get(o);
    return node != null && node.parent == this;
  }

  @Override
  public boolean remove(Object o) {
    return unlink(o) != null;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return randomAccessMap.keySet().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    for (E e : c) {
      add(e);
    }
    return true;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    for (Object e : c) {
      remove(e);
    }
    return true;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void clear() {
    head = null;
    tail = null;
  }

  public E get(Object o) {
    Node<E> node = randomAccessMap.get(o);
    if (node != null) {
      return node.item;
    }
    return null;
  }

  private E unlink(Object o) {
    Node<E> element = randomAccessMap.remove(o);
    if (element == null) {
      throw new NoSuchElementException();
    }
    if (element.prev != null) {
      element.prev.next = element.next;
    }
    if (element.next != null) {
      element.next.prev = element.prev.next;
    }
    if (element == head) {
      head = element.next;
    }
    if (element == tail) {
      tail = element.prev;
    }
    size --;
    modCount ++;
    return element.item;
  }

  private void linkAfter(E e, Node<E> afterNode) {
    if (afterNode == null) {
      throw new NoSuchElementException();
    }
    Node<E> node = new Node<>(this, e, afterNode);
    if (afterNode.next != null) {
      node.next = afterNode.next;
      node.next.prev = node;
    }
    afterNode.next = node;
    randomAccessMap.put(e, node);
    size ++;
    modCount ++;
  }

  private void linkHead(E e) {
    if (head == null) {
      head = new Node<>(this, e, null);
      tail = head;
    } else {
      Node<E> newNode = new Node<>(this, e, null);
      newNode.next = head;
      head.prev = newNode;
      head = newNode;
    }
    randomAccessMap.put(e, head);
    size ++;
    modCount ++;
  }

  private void checkForComodification(int expectedModCount) {
    if (modCount != expectedModCount) {
      throw new ConcurrentModificationException("List size has changed");
    }
  }

  @Override
  public ListIterator<E> listIterator() {
    return new ListItr(head.item);
  }

  @Override
  public Iterator<E> iterator() {
    return new ListItr(head.item);
  }

  public Iterator<E> iterator(E from) {
    return new ListItr(from);
  }

  public ListIterator<E> listIterator(E from) {
    return new ListItr(from);
  }

  private class ListItr implements ListIterator<E> {
    private Node<E> lastReturned;
    private Node<E> next;
    private int expectedModCount = modCount;

    public ListItr(E from) {
      Node<E> node = randomAccessMap.get(from);
      if (node.parent != DoubleLinkedList.this) {
        throw new IllegalArgumentException("Element " + from + " does not belong to this linked list.");
      }
      this.next = node;
    }

    @Override
    public boolean hasNext() {
      checkForComodification(expectedModCount);
      return next != null;
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      checkForComodification(expectedModCount);
      E result = null;
      result = next.item;
      lastReturned = next;
      next = next.next;
      return result;
    }

    @Override
    public boolean hasPrevious() {
      checkForComodification(expectedModCount);
      return next != null && next.prev != null;
    }

    @Override
    public E previous() {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      checkForComodification(expectedModCount);
      E result = null;
      result = next.item;
      lastReturned = next;
      next = next.prev;
      return result;
    }

    @Override
    public int nextIndex() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int previousIndex() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {
      checkForComodification(expectedModCount);
      unlink(lastReturned.item);
      lastReturned = null;
      expectedModCount ++;
    }

    @Override
    public void set(E e) {
      if (lastReturned == null) {
        throw new IllegalStateException();
      }
      checkForComodification(expectedModCount);
      lastReturned.item = e;
    }

    @Override
    public void add(E e) {
      checkForComodification(expectedModCount);
      if (lastReturned == null) {
        linkHead(e);
      } else {
        linkAfter(e, lastReturned);
      }
      lastReturned = null;
      expectedModCount ++;
    }
  }

  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public E get(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public E set(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public E remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int lastIndexOf(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    Node<E> print = head;
    StringBuilder sb = new StringBuilder("RandomAccessDoubleLinkedList: \n");
    while (print != null) {
      sb.append("Node: ");
      sb.append(print.item);
      sb.append(" Prev: ");
      if (print.prev != null) {
        sb.append(print.prev.item);
      }
      sb.append(" Next: ");
      if (print.next != null) {
        sb.append(print.next.item);
      }
      sb.append("\n");
      print = print.next;
    }
    sb.append(size());
    return sb.toString();
  }

  @Override
  public Object[] toArray() {
    int checkSize = size;
    Object[] result = new Object[size];
    int i = 0;
    Node<E> node = head;
    while (node != null) {
      checkForComodification(checkSize);
      result[i++] = node.item;
      node = node.next;
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    int checkSize = size;
    if (a.length < randomAccessMap.size()) {
      a =
          (T[])
              java.lang.reflect.Array.newInstance(
                  a.getClass().getComponentType(), size);
    }
    int i = 0;
    Object[] result = a;
    for (Node<E> x = head; x != null; x = x.next) {
      checkForComodification(checkSize);
      result[i++] = x.item;
    }

    if (a.length > size) {
      a[size] = null;
    }

    return a;
  }

  @Override
  public boolean equals(Object other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }
}
