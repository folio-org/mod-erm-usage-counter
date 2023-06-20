package org.olf.erm.usage.counter41.adapters;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Test;

public class AdaptersTest {

  @Test
  public void testZonedDateTimeAdapter() {
    String input = "2018-10-24T08:37:25.730+01:00";
    ZonedDateTimeAdapter zonedDateTimeAdapter = new ZonedDateTimeAdapter();

    ZonedDateTime zonedDateTime = zonedDateTimeAdapter.unmarshal(input);
    String str = zonedDateTimeAdapter.marshal(zonedDateTime);

    assertThat(zonedDateTime)
        .isEqualTo(ZonedDateTime.of(2018, 10, 24, 8, 37, 25, 730000000, ZoneOffset.ofHours(1)));
    assertThat(str).isEqualTo(input);
  }

  @Test
  public void testLocalDateTimeAdapter() {
    String input = "2018-10-24";
    LocalDateAdapter localDateAdapter = new LocalDateAdapter();

    LocalDate localDate = localDateAdapter.unmarshal(input);
    String str = localDateAdapter.marshal(localDate);

    assertThat(localDate).isEqualTo(LocalDate.of(2018, 10, 24));
    assertThat(str).isEqualTo(input);
  }
}
