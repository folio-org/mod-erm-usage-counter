package org.olf.erm.usage.counter41.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");

  @Override
  public ZonedDateTime unmarshal(String v) {
    if (v == null) return null;
    return ZonedDateTime.parse(v, formatter);
  }

  @Override
  public String marshal(ZonedDateTime v) {
    if (v == null) return null;
    return v.format(formatter);
  }
}
