package linkedlist.partitioned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import client.Item;

class PartitionedLinkedListTest {

  private PartitionedLinkedList<LocalDateTime, Item> list = new PartitionedLinkedList<>(new PartitionIndexFactory<LocalDateTime>() {

    private LocalTime startOfDay = LocalTime.of(6, 0);

    @Override
    public int maxPartitionCount() {
      return 8;
    }

    @Override
    public PartitionIndex<LocalDateTime> newPartitionIndex(LocalDateTime index) {
      return new PartitionIndex<>(index.with(startOfDay), index.with(startOfDay).plusHours(24));
    }});

  @Test
  void addItems() {
    list.add(new Item(LocalDateTime.now()));
    assertEquals(1, list.partitionCount());
    list.add(new Item(LocalDateTime.now().plusHours(48)));
    assertEquals(2, list.partitionCount());
    list.add(new Item(LocalDateTime.now().minusHours(48)));
    assertEquals(3, list.partitionCount());
    list.add(new Item(LocalDateTime.now().plusHours(24)));
    list.add(new Item(LocalDateTime.now().plusHours(23)));
    list.add(new Item(LocalDateTime.now().plusHours(22)));
    assertEquals(4, list.partitionCount());

    assertEquals(6, list.size());
  }
}
