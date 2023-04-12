package client;

import java.time.LocalDateTime;
import linkedlist.partitioned.Indexed;

/*
 * This will be a sequence in a channel service real world example.
 */
public class Item implements Indexed<LocalDateTime> {

  private final LocalDateTime startDateTime;
  private static int count = 0;

  public Item(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
    count ++;
  }

  public static int getCount() {
    return count;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public LocalDateTime index() {
    return startDateTime;
  }

}
