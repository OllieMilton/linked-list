package linkedlist;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RandomAccessDoubleLinkedList<E> implements Collection<E> {

  private Node head;
  private Node tail;
  private final Map<E, Node> randomAccessMap = new HashMap<>();

  private class Node {
    Node prev;
    E item;
    Node next;

    Node(E item, Node prev) {
      this.item = item;
      this.prev = prev;
    }
  }

  @Override
  public boolean add(E e) {
    if (head == null) {
      head = new Node(e, null);
      tail = head;
    } else {
      Node after = tail;
      tail = new Node(e, tail);
      if (after != null) {
        after.next = tail;
      }
      tail.prev = after;
    }
    randomAccessMap.put(e, tail);
    return true;
  }

  @Override
  public Iterator<E> iterator() {
    return iterator(head.item);
  }

  public Iterator<E> iterator(E from) {
    return new Iterator<E>() {
      int checkSize = randomAccessMap.size();
      Node current = randomAccessMap.get(from);

      @Override
      public boolean hasNext() {
        checkForComodification(checkSize);
        return current != null;
      }

      @Override
      public E next() {
        checkForComodification(checkSize);
        E result = null;
        if (current != null) {
          result = current.item;
          current = current.next;
        }
        return result;
      }
    };
  }

  @Override
  public int size() {
    return randomAccessMap.size();
  }

  @Override
  public boolean isEmpty() {
    return randomAccessMap.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return randomAccessMap.containsKey(o);
  }

  @Override
  public Object[] toArray() {
    int checkSize = randomAccessMap.size();
    Object[] result = new Object[randomAccessMap.size()];
    int i = 0;
    Node node = head;
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
    int checkSize = randomAccessMap.size();
    if (a.length < randomAccessMap.size()) {
      a =
          (T[])
              java.lang.reflect.Array.newInstance(
                  a.getClass().getComponentType(), randomAccessMap.size());
    }
    int i = 0;
    Object[] result = a;
    for (Node x = head; x != null; x = x.next) {
      checkForComodification(checkSize);
      result[i++] = x.item;
    }

    if (a.length > randomAccessMap.size()) {
      a[randomAccessMap.size()] = null;
    }

    return a;
  }

  @Override
  public boolean remove(Object o) {
    Node remove = randomAccessMap.remove(o);
    if (remove != null) {
      if (remove.prev != null) {
        remove.prev.next = remove.next;
      }
      if (remove.next != null) {
        remove.next.prev = remove.prev.next;
      }
      if (remove == head) {
        head = remove.next;
      }
      if (remove == tail) {
        tail = remove.prev;
      }
      return true;
    }
    return false;
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
    return false;
  }

  @Override
  public void clear() {
    randomAccessMap.clear();
    head = null;
    tail = null;
  }

  public E get(Object o) {
    Node node = randomAccessMap.get(o);
    if (node != null) {
      return node.item;
    }
    return null;
  }

  public void insertAfter(E after, E item) {
    Node afterNode = randomAccessMap.get(after);
    if (afterNode == null) {
      throw new IllegalArgumentException("Could not find list node for [" + after + "]");
    }
    Node node = new Node(item, afterNode);
    if (afterNode.next != null) {
      node.next = afterNode.next;
      node.next.prev = node;
    }
    afterNode.next = node;
    randomAccessMap.put(item, node);
  }

  public void replace(E was, E item) {
    Node itemNode = randomAccessMap.get(was);
    if (itemNode == null) {
      throw new IllegalArgumentException("Could not find list node for [" + was + "]");
    }
    itemNode.item = item;
  }

  public void replace(E from, E to, Collection<E> items) {
    Node fromNode = randomAccessMap.get(from);
    Node toNode = randomAccessMap.get(to);
    if (fromNode == null || toNode == null) {
      throw new IllegalArgumentException("Could not find list node for from or to");
    }
    // disconnect the old block
    if (fromNode.next != null) {
      fromNode.next.prev = null;
    }
    if (toNode.prev != null) {
      toNode.prev.next = null;
    }
    // add new, initialise node to from node which simply confirms the list to from and to if items is empty
    Node node = fromNode;
    Node prev = fromNode;
    for (E item : items) {
      node = new Node(item, prev);
      prev.next = node;
      prev = node;
      randomAccessMap.put(item, node);
    }
    toNode.prev = node;
  }

  @Override
  public String toString() {
    Node print = head;
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

  private void checkForComodification(int checkSize) {
    if (randomAccessMap.size() != checkSize) {
      throw new ConcurrentModificationException("List size has changed");
    }
  }
}
