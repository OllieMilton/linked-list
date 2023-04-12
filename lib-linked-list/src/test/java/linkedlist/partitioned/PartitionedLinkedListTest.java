package linkedlist.partitioned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import client.Item;

class PartitionedLinkedListTest {

  private PartitionedLinkedList<LocalDateTime, Item> list = new PartitionedLinkedList<>(new PartitionIndexFactory<LocalDateTime>() {

    // partition at 6am every day
    private LocalTime startOfDay = LocalTime.of(6, 0);

    // limit the number of partitions to 8 and therefore 8 days
    @Override
    public int maxPartitionCount() {
      return 8;
    }

    @Override
    public PartitionIndex<LocalDateTime> newPartitionIndex(LocalDateTime index) {
      // create a new partition starting at 6 am on the date of the incoming sequence that covers 24 hours
      // note, this is a bit naive as it stands as and needs to take the incoming time into account
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
