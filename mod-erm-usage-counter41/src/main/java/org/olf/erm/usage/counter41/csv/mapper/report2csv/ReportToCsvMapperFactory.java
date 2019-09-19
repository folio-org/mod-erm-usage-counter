package org.olf.erm.usage.counter41.csv.mapper.report2csv;

import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.lang3.ObjectUtils;
import org.niso.schemas.counter.Report;
import org.olf.erm.usage.counter41.csv.mapper.MapperException;

public class ReportToCsvMapperFactory {
  private static final String[] BR1 = new String[] {"BR1", "Book Report 1"};
  private static final String[] BR2 = new String[] {"BR2", "Book Report 2"};
  private static final String[] DB1 = new String[] {"DB1", "Database Report 1"};
  private static final String[] JR1 = new String[] {"JR1", "Journal Report 1"};
  private static final String[] PR1 = new String[] {"PR1", "Platform Report 1"};

  public static ReportToCsvMapper createCSVMapper(Report report) throws MapperException {
    Objects.requireNonNull(report.getVersion());
    String title = ObjectUtils.firstNonNull(report.getTitle(), report.getName(), report.getID());
    Objects.requireNonNull(title);

    if (report.getVersion().equals("4")) {
      if (Stream.of(JR1).anyMatch(title::contains)) {
        return new JR1(report);
      }
      if (Stream.of(DB1).anyMatch(title::contains)) {
        return new DB1(report);
      }
      if (Stream.of(BR2).anyMatch(title::contains)) {
        return new BR2(report);
      }
      if (Stream.of(PR1).anyMatch(title::contains)) {
        return new PR1(report);
      }
      if (Stream.of(BR1).anyMatch(title::contains)) {
        return new BR1(report);
      }
    }
    throw new MapperException(
        String.format(
            "No mapping found for report title '%s' and version '%s'",
            report.getTitle(), report.getVersion()));
  }

  private ReportToCsvMapperFactory() {}
}
