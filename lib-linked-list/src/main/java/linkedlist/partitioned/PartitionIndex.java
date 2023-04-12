package linkedlist.partitioned;

import java.util.Objects;

public record PartitionIndex<I extends Comparable<? super I>>(I min, I max) implements Comparable<I>{

  public PartitionIndex(I min, I max) {
    this.max = max;
    this.min = min;
    if (min.compareTo(max) >= 0) {
      throw new IllegalArgumentException("min MUST be less than max");
    }
  }

  @Override
  public int compareTo(I obj) {
    Objects.requireNonNull(obj);
    int minCmp = obj.compareTo(min);
    int maxCmp = obj.compareTo(max);
    if (minCmp < 0) {
      return minCmp;
    } else if (minCmp >= 0 && maxCmp <= 0) {
      return 0;
    } else {
      return maxCmp;
    }
  }
}
