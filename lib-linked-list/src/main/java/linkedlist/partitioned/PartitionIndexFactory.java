package linkedlist.partitioned;

public interface PartitionIndexFactory<I extends Comparable<? super I>> {

  /**
   * Creates a new {@link PartitionIndex} for the given index. Partition count is supplied so that implementations can throw an exception if a maximum number of partitions has been reached.
   * @param partitionCount the number of existing partitions.
   * @param index the index to create the partition for.
   * @return The newly created {@link PartitionIndex}
   */
  PartitionIndex<I> newPartitionIndex(int partitionCount, I index);
}
