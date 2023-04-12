package linkedlist.partitioned;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class PartitionedLinkedList<I extends Comparable<? super I>, E extends Indexed<I>> {

  private class Partition {
    final PartitionIndex<I> index;
    final Collection<E> list = new DoubleLinkedList();

    Partition(PartitionIndex<I> index) {
      this.index = index;
    }
  }

  class Node {
    Node prev;
    E item;
    Node next;

    Node(E item, Node prev) {
      this.item = item;
      this.prev = prev;
    }
  }

  private final LinkedList<Partition> partitionList = new LinkedList<>();
  private final Map<E, Node> randomAccessMap = new HashMap<>();
  private final PartitionIndexFactory<I> partitionIdxFactory;

  public PartitionedLinkedList(PartitionIndexFactory<I> partitionIdxFactory) {
    Objects.requireNonNull(partitionIdxFactory);
    this.partitionIdxFactory = partitionIdxFactory;
  }

  public int size() {
    return randomAccessMap.size();
  }

  public boolean isEmpty() {
    return randomAccessMap.isEmpty();
  }

  public boolean contains(Object o) {
    return randomAccessMap.containsKey(o);
  }

  public Iterator<E> elementIterator() {
    return new Iterator<E>() {
      Iterator<Partition> partitions = partitionList.iterator();
      Partition partition = null;
      Iterator<E> listItr;

      @Override
      public boolean hasNext() {
        if (partitions.hasNext() && listItr == null) {
          partition = partitions.next();
          listItr = partition.list.iterator();
        }
        return partition != null && listItr.hasNext();
      }


      @Override
      public E next() {
        E result = null;
        if (listItr != null && listItr.hasNext()) {
          // return next item from the vertical list
          result = listItr.next();
          if (!listItr.hasNext()) {
            // we have been over the entire vertical list so move to the next horizontal node
            listItr = null;
          }
        } else {
          throw new NoSuchElementException();
        }
        return result;
      }
    };
  }

  public void clear() {
    randomAccessMap.clear();
    partitionList.stream().forEach(p -> p.list.clear());
  }

  public boolean add(E item) {
    return findPartition(item).list.add(item);
  }

  int partitionCount() {
    return partitionList.size();
  }

  private Partition findPartition(E item) {
    ListIterator<Partition> itr = partitionList.listIterator();
    if (!itr.hasNext()) {
      Partition result = new Partition(partitionIdxFactory.newPartitionIndex(item.index()));
      itr.add(result);
      return result;
    }
    while (itr.hasNext()) {
      Partition partition = itr.next();
      int cmp = partition.index.compareTo(item.index());
      if (cmp < 0) {
        itr.previous();
        Partition result = new Partition(partitionIdxFactory.newPartitionIndex(item.index()));
        itr.add(result);
        return result;
      } else if (cmp == 0) {
        return partition;
      } else {
        if (!itr.hasNext()) {
          Partition result = new Partition(partitionIdxFactory.newPartitionIndex(item.index()));
          itr.add(result);
          return result;
        }
      }
    }
    throw new IndexOutOfBoundsException("Could not find Partition");
  }

  class DoubleLinkedList implements Collection<E> {

    private Node head;
    private Node tail;

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
          } else {
            throw new NoSuchElementException();
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
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void clear() {
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

}
