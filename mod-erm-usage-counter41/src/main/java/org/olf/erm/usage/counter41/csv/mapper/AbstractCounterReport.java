package org.olf.erm.usage.counter41.csv.mapper;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.PerformanceCounter;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.Counter4Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

public abstract class AbstractCounterReport {

  public Report report;
  public List<YearMonth> yearMonths;
  final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("MMM-uuuu", Locale.ENGLISH);

  public abstract String[] getHeader();

  public abstract String[] getFieldMapping();

  public abstract String getTitle();

  public abstract String getDescription();

  private String[] createHeader() {
    Stream<String> header = Arrays.stream(getHeader());
    Stream<String> months = yearMonths.stream().map(ym -> ym.format(formatter));
    return Stream.concat(header, months).toArray(String[]::new);
  }

  private String[] createFieldMapping() {
    Stream<String> mapping = Arrays.stream(getFieldMapping());
    Stream<String> months = Collections.nCopies(yearMonths.size(), "itemPerformance").stream();
    return Stream.concat(mapping, months).toArray(String[]::new);
  }

  private String createReportHeader() {
    StringWriter stringWriter = new StringWriter();

    try (CsvListWriter csvListWriter =
        new CsvListWriter(stringWriter, CsvPreference.STANDARD_PREFERENCE)) {
      csvListWriter.write(getTitle(), getDescription());
      csvListWriter.write(report.getCustomer().get(0).getID());
      csvListWriter.write(
          ""); // FIXME: Cell A3 contains the “Institutional Identifier” as defined in Appendix A,
      // but may be left blank if the vendor does not use Institutional Identifiers
      csvListWriter.write("Period covered by Report");
      csvListWriter.write(
          Iterables.getFirst(yearMonths, null).atDay(1).toString()
              + " to "
              + Iterables.getLast(yearMonths).atEndOfMonth().toString());
      csvListWriter.write("Date run");
      csvListWriter.write(LocalDate.now());
      csvListWriter.flush();
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return "";
    }
    return stringWriter.toString();
  }

  public String[] createTotals() {
    return null;
  }

  public abstract void writeItems(ICsvDozerBeanWriter writer) throws IOException;

  public final String getSinglePublisher() {
    List<String> uniquePublishers =
        report.getCustomer().get(0).getReportItems().stream()
            .map(ReportItem::getItemPublisher)
            .distinct()
            .collect(Collectors.toList());
    LOG.info("Found {} publishers: {}", uniquePublishers.size(), uniquePublishers);
    return (uniquePublishers.size() == 1) ? uniquePublishers.get(0) : null;
  }

  public final String getSinglePlatform() {
    List<String> uniquePlatforms =
        report.getCustomer().get(0).getReportItems().stream()
            .map(ReportItem::getItemPlatform)
            .distinct()
            .collect(Collectors.toList());
    LOG.info("Found {} platforms: {}", uniquePlatforms.size(), uniquePlatforms);
    return (uniquePlatforms.size() == 1) ? uniquePlatforms.get(0) : null;
  }

  public final String bigIntToStringOrNull(BigInteger bigint) {
    return (bigint == null) ? null : String.valueOf(bigint);
  }

  public final BigInteger getPeriodMetricTotal(MetricType metricType, YearMonth month) {
    return report.getCustomer().get(0).getReportItems().stream()
        .flatMap(ri -> ri.getItemPerformance().stream())
        .filter(
            ip ->
                month == null
                    || (ip.getPeriod()
                            .getBegin()
                            .toGregorianCalendar()
                            .toZonedDateTime()
                            .toLocalDate()
                            .equals(month.atDay(1))
                        && ip.getPeriod()
                            .getEnd()
                            .toGregorianCalendar()
                            .toZonedDateTime()
                            .toLocalDate()
                            .equals(month.atEndOfMonth())))
        .flatMap(m -> m.getInstance().stream())
        .filter(pc -> pc.getMetricType().equals(metricType))
        .map(PerformanceCounter::getCount)
        .reduce(BigInteger::add)
        .orElse(null);
  }

  /**
   * Returns a CSV representation of the report
   *
   * @return CSV formatted String
   */
  public String toCSV() {
    StringWriter stringWriter = new StringWriter();

    stringWriter.append(createReportHeader());

    // and the formatted stuff with CsvDozerBeanWriter
    try (ICsvDozerBeanWriter beanWriter =
        new CsvDozerBeanWriter(stringWriter, CsvPreference.STANDARD_PREFERENCE)) {

      // configure the mapping from the fields to the CSV columns
      beanWriter.configureBeanMapping(ReportItem.class, createFieldMapping());

      // write the header
      beanWriter.writeHeader(createHeader());

      // write totals line
      String[] totals = createTotals();
      if (totals != null) beanWriter.writeHeader(totals);

      writeItems(beanWriter);

      beanWriter.flush();
      return stringWriter.toString();
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  };

  /**
   * Returns a seamless list of every month that falls within the reporting period.
   *
   * @return list of {@code YearMonth}
   */
  private List<YearMonth> getYearMonths() {
    List<YearMonth> uniqueSortedYearMonths = Counter4Utils.getYearMonthsFromReport(report);
    if (uniqueSortedYearMonths.isEmpty()) {
      return uniqueSortedYearMonths;
    } else {
      YearMonth first = uniqueSortedYearMonths.get(0);
      YearMonth last = uniqueSortedYearMonths.get(uniqueSortedYearMonths.size() - 1);
      long diff = first.until(last, ChronoUnit.MONTHS);
      return Stream.iterate(first, next -> next.plusMonths(1))
          .limit(diff + 1)
          .collect(Collectors.toList());
    }
  }

  public AbstractCounterReport(Report report) {
    this.report = report;
    this.yearMonths = getYearMonths();
  }
}