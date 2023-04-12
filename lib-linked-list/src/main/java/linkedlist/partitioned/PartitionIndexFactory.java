package linkedlist.partitioned;

public interface PartitionIndexFactory<I extends Comparable<? super I>> {

  int maxPartitionCount();

  PartitionIndex<I> newPartitionIndex(I index);
}
