package org.olf.erm.usage.counter41.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

  @Override
  public LocalDate unmarshal(String v) {
    if (v == null) return null;
    return LocalDate.parse(v, formatter);
  }

  @Override
  public String marshal(LocalDate v) {
    if (v == null) return null;
    return v.format(formatter);
  }
}
