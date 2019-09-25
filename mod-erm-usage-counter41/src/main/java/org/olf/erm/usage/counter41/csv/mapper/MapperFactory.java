package org.olf.erm.usage.counter41.csv.mapper;

import com.google.common.base.Strings;
import java.util.stream.Stream;
import org.apache.commons.lang3.ObjectUtils;
import org.niso.schemas.counter.Report;
import org.olf.erm.usage.counter41.csv.mapper.csv2report.BR1ToReport;
import org.olf.erm.usage.counter41.csv.mapper.csv2report.BR2ToReport;
import org.olf.erm.usage.counter41.csv.mapper.csv2report.CsvToReportMapper;
import org.olf.erm.usage.counter41.csv.mapper.csv2report.DB1ToReport;
import org.olf.erm.usage.counter41.csv.mapper.csv2report.JR1ToReport;
import org.olf.erm.usage.counter41.csv.mapper.csv2report.PR1ToReport;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.BR1;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.BR2;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.DB1;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.JR1;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.PR1;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.ReportToCsvMapper;

public class MapperFactory {
  private static final String[] BR1 = new String[] {"BR1", "Book Report 1"};
  private static final String[] BR2 = new String[] {"BR2", "Book Report 2"};
  private static final String[] DB1 = new String[] {"DB1", "Database Report 1"};
  private static final String[] JR1 = new String[] {"JR1", "Journal Report 1"};
  private static final String[] PR1 = new String[] {"PR1", "Platform Report 1"};

  public static ReportToCsvMapper createCSVMapper(Report report) throws MapperException {
    String title = ObjectUtils.firstNonNull(report.getTitle(), report.getName(), report.getID());
    String version = report.getVersion();
    if (Strings.isNullOrEmpty(version) || Strings.isNullOrEmpty(title)) {
      throw new MapperException("Report missing report version and/or title");
    }
    if (version.equals("4")) {
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
        String.format("No mapping found for report title '%s' and version '%s'", title, version));
  }

  public static CsvToReportMapper createCsvToReportMapper(String csvString) throws MapperException {
    if (csvString.startsWith("Journal Report 1 (R4)")) {
      return new JR1ToReport(csvString);
    } else if (csvString.startsWith("Platform Report 1 (R4)")) {
      return new PR1ToReport(csvString);
    } else if (csvString.startsWith("Database Report 1 (R4)")) {
      return new DB1ToReport(csvString);
    } else if (csvString.startsWith("Book Report 1 (R4)")) {
      return new BR1ToReport(csvString);
    } else if (csvString.startsWith("Book Report 2 (R4)")) {
      return new BR2ToReport(csvString);
    }
    throw new MapperException("Report type not supported");
  }

  private MapperFactory() {}
}
