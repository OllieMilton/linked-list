package linkedlist.partitioned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import client.Item;

class PartitionedLinkedListTest {

  // partition at 6am every day
  private LocalTime startOfDay = LocalTime.of(6, 0);

  // create a new partition starting at 6 am on the date of the incoming sequence that covers 24 hours
  // note, this is a bit naive as it stands as and needs to take the incoming index time into account
  private PartitionedLinkedList<LocalDateTime, Item> list = new PartitionedLinkedList<>((e, i) -> new PartitionIndex<LocalDateTime>(i.with(startOfDay), i.with(startOfDay).plusHours(24)));

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
    List<Item> arraylist = new ArrayList<>();
    list.stream().forEach(i -> arraylist.add(i));

    assertEquals(6, arraylist.size());
  }
}
