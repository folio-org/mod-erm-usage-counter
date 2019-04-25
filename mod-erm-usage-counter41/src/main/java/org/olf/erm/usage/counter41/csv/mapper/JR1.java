package org.olf.erm.usage.counter41.csv.mapper;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.PerformanceCounter;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.csv.cellprocessor.IdentifierProcessor;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceProcessor;
import org.olf.erm.usage.counter41.csv.cellprocessor.ReportingPeriodProcessor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

public class JR1 extends AbstractCounterReport {

  public JR1(Report report) {
    super(report);
  }

  @Override
  public String[] getHeader() {
    return new String[] {
      "Journal",
      "Publisher",
      "Platform",
      "Journal DOI",
      "Proprietary Identifier",
      "Print ISSN",
      "Online ISSN",
      "Reporting Period Total",
      "Reporting Period HTML",
      "Reporting Period PDF"
    };
  }

  @Override
  public String[] getFieldMapping() {
    return new String[] {
      "itemName",
      "itemPublisher",
      "itemPlatform",
      "itemPlatform",
      "itemPlatform",
      "itemIdentifier",
      "itemIdentifier",
      "itemPerformance",
      "itemPerformance",
      "itemPerformance"
    };
  }

  @Override
  public String getTitle() {
    return "Journal Report 1(R4)";
  }

  @Override
  public String getDescription() {
    return "Number of Successful Full-Text Article Requests by Month and Journal";
  }

  private CellProcessor[] createProcessors() {
    List<Optional> first =
        Arrays.asList(
            new Optional(), // Journal
            new Optional(), // Publisher
            new Optional(), // Platform
            new Optional(), // Journal DOI
            new Optional(), // Proprietary Identifier
            new Optional(new IdentifierProcessor("Print_ISSN")), // Print ISSN
            new Optional(new IdentifierProcessor("Online_ISSN")), // Online ISSN
            new Optional(
                new ReportingPeriodProcessor(MetricType.FT_TOTAL)), // Reporting Period Total
            new Optional(new ReportingPeriodProcessor(MetricType.FT_HTML)), // Reporting Period HTML
            new Optional(new ReportingPeriodProcessor(MetricType.FT_PDF)) // Reporting Period PDF
            );
    Stream<Optional> rest =
        yearMonths.stream()
            .map(ym -> new Optional(new MonthPerformanceProcessor(ym, MetricType.FT_TOTAL)));
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }

  private String getSinglePublisher() {
    List<String> uniquePublishers =
        report.getCustomer().get(0).getReportItems().stream()
            .map(ReportItem::getItemPublisher)
            .distinct()
            .collect(Collectors.toList());
    LOG.info(String.format("Found %s publishers: %s", uniquePublishers.size(), uniquePublishers));
    return (uniquePublishers.size() == 1) ? uniquePublishers.get(0) : null;
  }

  private String getSinglePlatform() {
    List<String> uniquePlatforms =
        report.getCustomer().get(0).getReportItems().stream()
            .map(ReportItem::getItemPlatform)
            .distinct()
            .collect(Collectors.toList());
    LOG.info(String.format("Found %s platforms: %s", uniquePlatforms.size(), uniquePlatforms));
    return (uniquePlatforms.size() == 1) ? uniquePlatforms.get(0) : null;
  }

  private BigInteger getPeriodMetricTotal(MetricType metricType, YearMonth month) {
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
        .reduce((a, b) -> a.add(b))
        .orElse(null);
  }

  private String bigIntToStringOrNull(BigInteger bigint) {
    return (bigint == null) ? null : String.valueOf(bigint);
  }

  private String[] createTotals() {
    List<String> first =
        Arrays.asList(
            "Total for all journals",
            Strings.emptyToNull(getSinglePublisher()),
            Strings.emptyToNull(getSinglePlatform()),
            null,
            null,
            null,
            null,
            bigIntToStringOrNull(getPeriodMetricTotal(MetricType.FT_TOTAL, null)),
            bigIntToStringOrNull(getPeriodMetricTotal(MetricType.FT_HTML, null)),
            bigIntToStringOrNull(getPeriodMetricTotal(MetricType.FT_PDF, null)));
    Stream<String> rest =
        yearMonths.stream()
            .map(ym -> bigIntToStringOrNull(getPeriodMetricTotal(MetricType.FT_TOTAL, ym)));
    return Stream.concat(first.stream(), rest).toArray(String[]::new);
  }

  @Override
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
      beanWriter.writeHeader(createTotals());

      // write journals
      CellProcessor[] processors = createProcessors();
      for (final ReportItem item : report.getCustomer().get(0).getReportItems()) {
        beanWriter.write(item, processors);
      }

      beanWriter.flush();
      return stringWriter.toString();
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }
}
