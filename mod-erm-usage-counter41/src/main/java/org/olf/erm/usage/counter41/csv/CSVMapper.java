package org.olf.erm.usage.counter41.csv;

import java.util.Objects;
import java.util.stream.Stream;
import org.niso.schemas.counter.Report;
import org.olf.erm.usage.counter41.csv.mapper.AbstractCounterReport;
import org.olf.erm.usage.counter41.csv.mapper.JR1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVMapper {

  private static final Logger LOG = LoggerFactory.getLogger(CSVMapper.class);
  private static final String[] JR1 = new String[] {"JR1", "Journal Report 1"};

  private static AbstractCounterReport getType(Report report) {
    Objects.requireNonNull(report.getVersion());
    Objects.requireNonNull(report.getTitle());

    if (report.getVersion().equals("4")) {
      String title = report.getTitle();
      if (Stream.of(JR1).anyMatch(title::contains)) {
        return new JR1(report);
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
