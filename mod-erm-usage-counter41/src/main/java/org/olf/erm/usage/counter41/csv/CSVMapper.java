package org.olf.erm.usage.counter41.csv;

import java.util.Objects;
import java.util.stream.Stream;
import org.niso.schemas.counter.Report;
import org.olf.erm.usage.counter41.csv.mapper.AbstractCounterReport;
import org.olf.erm.usage.counter41.csv.mapper.BR2;
import org.olf.erm.usage.counter41.csv.mapper.DB1;
import org.olf.erm.usage.counter41.csv.mapper.JR1;
import org.olf.erm.usage.counter41.csv.mapper.PR1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVMapper {

  private static final Logger LOG = LoggerFactory.getLogger(CSVMapper.class);
  private static final String[] BR2 = new String[] {"BR2", "Book Report 2"};
  private static final String[] DB1 = new String[] {"DB1", "Database Report 1"};
  private static final String[] JR1 = new String[] {"JR1", "Journal Report 1"};
  private static final String[] PR1 = new String[] {"PR1", "Platform Report 1"};

  private static AbstractCounterReport getType(Report report) {
    Objects.requireNonNull(report.getVersion());
    Objects.requireNonNull(report.getTitle());

    if (report.getVersion().equals("4")) {
      String title = report.getTitle();
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
    }
    return null;
  }

  public static String toCSV(Report report) {
    AbstractCounterReport type = getType(report);
    if (type != null) {
      return type.toCSV();
    } else {
      LOG.error(
          "No mapping found for report title '{}' and version '{}'",
          report.getTitle(),
          report.getVersion());
      return null;
    }
  }

  private CSVMapper() {}
}
