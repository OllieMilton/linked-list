package linkedlist.partitioned;

public interface PartitionIndexFactory<I extends Comparable<? super I>> {

  PartitionIndex<I> newPartitionIndex(int existingCount, I index);
}
