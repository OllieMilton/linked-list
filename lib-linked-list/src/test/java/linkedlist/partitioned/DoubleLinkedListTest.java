package linkedlist.partitioned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DoubleLinkedListTest {

  private List<String> list = new DoubleLinkedList<>(new HashMap<>());

  @BeforeEach
  void setup() {
    list.add("one");
    list.add("two");
  }

  @Test
  void addBeforeFirst() {
    ListIterator<String> itr = list.listIterator();

    while (itr.hasNext()) {
      if (!itr.hasPrevious()) {
        itr.add("three");
        break;
      }
    }

    assertEquals(3, list.size());
    Iterator<String> itr2 = list.iterator();
    assertEquals("three", itr2.next());
    assertEquals("one", itr2.next());
    assertEquals("two", itr2.next());
  }

  @Test
  void addAfterFirst() {
    ListIterator<String> itr = list.listIterator();

    while (itr.hasNext()) {
      String s = itr.next();
      if (s.equals("one")) {
        itr.add("three");
        break;
      }
    }

    assertEquals(3, list.size());
    Iterator<String> itr2 = list.iterator();
    assertEquals("one", itr2.next());
    assertEquals("three", itr2.next());
    assertEquals("two", itr2.next());
  }

  @Test
  void addLast() {
    ListIterator<String> itr = list.listIterator();

    while (itr.hasNext()) {
      String s = itr.next();
      if (s == "two") {
        itr.add("three");
        break;
      }
    }

    assertEquals(3, list.size());
    Iterator<String> itr2 = list.iterator();
    assertEquals("one", itr2.next());
    assertEquals("two", itr2.next());
    assertEquals("three", itr2.next());
  }
}
