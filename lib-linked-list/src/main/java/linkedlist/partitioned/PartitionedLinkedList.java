package linkedlist.partitioned;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import linkedlist.partitioned.DoubleLinkedList.Node;

/**
 * A linked list where partitions can be used to contain list item is sub lists.
 * <br/>
 * Every list item must extend {@link Indexed} to enable partitions to be selected
 * by comparator which using the value returned from {@code Indexed.index()}
 * <br/>
 * An implementation of {@link PartitionIndexFactory} must be supplied to
 * {@link PartitionedLinkedList} so that each usage can define how each partition
 * is generated as well as specifying a maximum number of partitions.
 * <br/>
 * A two dimensional linked list is used where the horizontal list makes the list
 * of partitions then each partition contains it's own linked list.
 *
 * <pre>
 * ┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐
 * │  par 1  ├────►│  par 2  ├────►│  par 3  ├────►│  par 1  │
 * └────┬────┘     └────┬────┘     └────┬────┘     └────┬────┘
 *      │               │               │               │
 *      ▼               ▼               ▼               ▼
 *   ┌─────┐         ┌─────┐         ┌─────┐         ┌─────┐
 *   │  e1 │         │  e1 │         │  e1 │         │  e1 │
 *   └──┬──┘         └──┬──┘         └──┬──┘         └──┬──┘
 *      │               │               │               │
 *      ▼               ▼               ▼               ▼
 *   ┌─────┐         ┌─────┐         ┌─────┐         ┌─────┐
 *   │  e2 │         │  e2 │         │  e2 │         │  e2 │
 *   └──┬──┘         └──┬──┘         └─────┘         └──┬──┘
 *      │               │                               │
 *      ▼               ▼                               ▼
 *   ┌─────┐         ┌─────┐                         ┌─────┐
 *   │  e3 │         │  e3 │                         │  e3 │
 *   └──┬──┘         └─────┘                         └──┬──┘
 *      │                                               │
 *      ▼                                               ▼
 *   ┌─────┐                                         ┌─────┐
 *   │  e4 │                                         │  e4 │
 *   └─────┘                                         └──┬──┘
 *                                                      │
 *                                                      ▼
 *                                                   ┌─────┐
 *                                                   │  e5 │
 *                                                   └─────┘
 * </pre>
 *
 * @param <I> the generic type of the object used to index each list item into a partition.
 * @param <E extends Indexed<I>> the generic type of the list items.
 */
public class PartitionedLinkedList<I extends Comparable<? super I>, E extends Indexed<I>> {

  private class Partition {
    final PartitionIndex<I> index;
    final DoubleLinkedList<E> list = new DoubleLinkedList<>(randomAccessMap);

    Partition(PartitionIndex<I> index) {
      this.index = index;
    }
  }

  private final LinkedList<Partition> partitionList = new LinkedList<>();
  private final Map<E, Node<E>> randomAccessMap = new HashMap<>();
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
        return partition != null && partitions.hasNext() && listItr.hasNext();
      }


      @Override
      public E next() {
        if (listItr == null || !listItr.hasNext()) {
          throw new NoSuchElementException();
        }
        // return next item from the vertical list
        E result = listItr.next();
        if (!listItr.hasNext()) {
          // we have been over the entire vertical list so move to the next horizontal node
          listItr = null;
        }
        return result;
      }
    };
  }

  public Stream<E> stream() {
    Spliterator<E> spliterator =
        Spliterators.spliteratorUnknownSize(
            elementIterator(), Spliterator.NONNULL | Spliterator.SORTED);
    return StreamSupport.stream(spliterator, false);
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

  public DoubleLinkedList<E> linkedList(Indexed<I> idx) {
    Partition partition = findExistingPartition(idx);
    if (partition == null) {
      throw new NoSuchElementException();
    }
    return partition.list;
  }

  private Partition findExistingPartition(Indexed<I> idx) {
    ListIterator<Partition> itr = partitionList.listIterator();
    while (itr.hasNext()) {
      Partition partition = itr.next();
      int cmp = partition.index.compareTo(idx.index());
      if (cmp == 0) {
        return partition;
      }
    }
    return null;
  }

  private Partition findPartition(Indexed<I> idx) {
    ListIterator<Partition> itr = partitionList.listIterator();
    if (!itr.hasNext()) {
      Partition result = new Partition(partitionIdxFactory.newPartitionIndex(partitionCount(), idx.index()));
      itr.add(result);
      return result;
    }
    while (itr.hasNext()) {
      Partition partition = itr.next();
      int cmp = partition.index.compareTo(idx.index());
      if (cmp < 0) {
        itr.previous();
        Partition result = new Partition(partitionIdxFactory.newPartitionIndex(partitionCount(), idx.index()));
        itr.add(result);
        return result;
      } else if (cmp == 0) {
        return partition;
      } else {
        if (!itr.hasNext()) {
          Partition result = new Partition(partitionIdxFactory.newPartitionIndex(partitionCount(), idx.index()));
          itr.add(result);
          return result;
        }
      }
    }
    throw new NoSuchElementException("Could not find Partition");
  }
}