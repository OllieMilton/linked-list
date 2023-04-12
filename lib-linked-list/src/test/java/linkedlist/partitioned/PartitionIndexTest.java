package linkedlist.partitioned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import linkedlist.partitioned.PartitionIndex;

class PartitionIndexTest {

  private PartitionIndex<Integer> hi = new PartitionIndex<>(5, 10);

  @Test
  void lessThanRange() {
    assertEquals(-1, hi.compareTo(1));
  }

  @Test
  void inRange() {
    assertEquals(0, hi.compareTo(5));
    assertEquals(0, hi.compareTo(6));
    assertEquals(0, hi.compareTo(7));
    assertEquals(0, hi.compareTo(8));
    assertEquals(0, hi.compareTo(9));
    assertEquals(0, hi.compareTo(10));
  }

  @Test
  void greaterThanRange() {
    assertEquals(1, hi.compareTo(11));
  }

  @Test
  void badRange() {
    assertThrows(IllegalArgumentException.class, () -> new PartitionIndex<Integer>(12, 10));
  }
}
