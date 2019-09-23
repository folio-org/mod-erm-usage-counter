package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import static org.apache.cxf.common.util.StringUtils.getFound;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.Report.Customer;
import org.niso.schemas.counter.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

public class ReportParser {

  private static final Logger log = LoggerFactory.getLogger(ReportParser.class);

  public Report fromCSV(String csvString) throws IOException, ReportMapperException {
    StringReader stringReader = new StringReader(csvString);
    List<String> lines = IOUtils.readLines(stringReader);

    if (lines.size() < 10) {
      throw new ReportMapperException("Invalid report supplied");
    }

    List<String> headerColumn = getHeaderColumn(lines.subList(0, 9));
    if (!headerColumn.get(0).equals("Journal Report 1 (R4)")) {
      throw new ReportMapperException("Report type not supported");
    }

    Report report = new Report();
    Customer customer = new Customer();
    report.getCustomer().add(customer);
    report.setTitle("Journal Report 1");
    report.setName("JR1");
    report.setVersion("4");
    // report.setID("");
    // report.setCreated("");

    customer.setID(headerColumn.get(1));
    customer.setName(headerColumn.get(2));

    if (!hasValidDates(headerColumn.get(4))) {
      throw new ReportMapperException("Invalid date range");
    }

    List<YearMonth> yearMonths = getYearMonths(headerColumn.get(4));

    List<ReportItem> reportItems =
        new JR1Mapper().getReportItems(lines.subList(9, lines.size()), yearMonths);
    customer.getReportItems().addAll(reportItems);
    return report;
  }

  private List<String> getHeaderColumn(List<String> header) {
    try (CsvListReader csvListReader =
        new CsvListReader(
            new StringReader(StringUtils.join(header, System.lineSeparator())),
            CsvPreference.STANDARD_PREFERENCE)) {
      List<String> headerColumn = new ArrayList<>();
      List<String> line;
      while ((line = csvListReader.read()) != null) {
        headerColumn.add(line.get(0));
      }
      return headerColumn;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private boolean hasValidDates(String dateRangeString) {
    try {
      List<LocalDate> dates =
          getFound(dateRangeString, "\\d{4}-\\d{2}-\\d{2}").stream()
              .map(LocalDate::parse)
              .collect(Collectors.toList());
      return dates.size() == 2
          && dates.get(0).getDayOfMonth() == 1
          && dates.get(1).getDayOfMonth()
              == YearMonth.from(dates.get(1)).atEndOfMonth().getDayOfMonth()
          && dates.get(0).compareTo(dates.get(1)) < 0;
    } catch (Exception e) {
      log.error("Error getting DateRange");
      return false;
    }
  }

  private List<YearMonth> getYearMonths(String dateRangeString) {
    List<String> strings = getFound(dateRangeString, "\\d{4}-\\d{2}");
    YearMonth start = YearMonth.parse(strings.get(0));
    YearMonth end = YearMonth.parse(strings.get(1));
    long diff = start.until(end, ChronoUnit.MONTHS);
    return Stream.iterate(start, next -> next.plusMonths(1))
        .limit(diff + 1)
        .collect(Collectors.toList());
  }

  public class ReportMapperException extends Exception {
    ReportMapperException(String message) {
      super(message);
    }
  }
}
