package org.olf.erm.usage.counter41.csv;

import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.lang3.ObjectUtils;
import org.niso.schemas.counter.Report;
import org.olf.erm.usage.counter41.csv.mapper.AbstractCounterReport;
import org.olf.erm.usage.counter41.csv.mapper.BR1;
import org.olf.erm.usage.counter41.csv.mapper.BR2;
import org.olf.erm.usage.counter41.csv.mapper.DB1;
import org.olf.erm.usage.counter41.csv.mapper.JR1;
import org.olf.erm.usage.counter41.csv.mapper.PR1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVMapper {

  private static final Logger LOG = LoggerFactory.getLogger(CSVMapper.class);
  private static final String[] BR1 = new String[] {"BR1", "Book Report 1"};
  private static final String[] BR2 = new String[] {"BR2", "Book Report 2"};
  private static final String[] DB1 = new String[] {"DB1", "Database Report 1"};
  private static final String[] JR1 = new String[] {"JR1", "Journal Report 1"};
  private static final String[] PR1 = new String[] {"PR1", "Platform Report 1"};

  private static AbstractCounterReport getType(Report report) {
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
