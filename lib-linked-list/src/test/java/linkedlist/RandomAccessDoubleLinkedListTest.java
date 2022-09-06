package linkedlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RandomAccessDoubleLinkedListTest {

  /**
   * Demo of an immutable sequence cache using a custom implementation of a double linked list with random access.
   *
   * Note, the linked list itself is mutable and therefore must still be protected with locks
   */

  // an immutable event. note, all params must be immutable or made so in the constructor
  record Event(String assetId, String eventType) {}

  // an immutable sequence. note, all params must be immutable or made so in the constructor
  record Sequence(String id, ZonedDateTime startDateTime, List<Event> events) {
    Sequence {
      events = Collections.unmodifiableList(events);
    }
  }

  @Test
  void immutablePlaylist() {
    // create mutable list - this will still need protection using locks
    RandomAccessDoubleLinkedList<Sequence> list = new RandomAccessDoubleLinkedList<>();

    // create immutable sequences
    Sequence one = new Sequence("1", ZonedDateTime.now(), Collections.singletonList(new Event("TEST-1", "program")));
    Sequence two = new Sequence("2", ZonedDateTime.now(), Collections.singletonList(new Event("TEST-1", "program")));
    Sequence three = new Sequence("3", ZonedDateTime.now(), Collections.singletonList(new Event("TEST-1", "program")));
    list.add(one);
    list.add(two);
    list.insertAfter(one, three);

    // check list size
    assertEquals(3, list.size());

    // check the linked list has ordered them correctly
    Iterator<Sequence> itr = list.iterator();
    assertEquals("1", itr.next().id());
    assertEquals("3", itr.next().id());
    assertEquals("2", itr.next().id());

    // prove that the event list is immutable
    assertThrows(UnsupportedOperationException.class, () -> one.events.add(new Event("JML","Commercial")));

    // compilation errors since records are immutable
    // one.id = "2";
    // one.id() = "2";

    // Update a node in the list with a new entry
    Sequence four = new Sequence("4", ZonedDateTime.now(), Collections.singletonList(new Event("TEST-1", "program")));
    list.put(one, four);

    // check list size
    assertEquals(3, list.size());

    // check the linked list has ordered them correctly
    itr = list.iterator();
    assertEquals("4", itr.next().id());
    assertEquals("3", itr.next().id());
    assertEquals("2", itr.next().id());
  }


}
