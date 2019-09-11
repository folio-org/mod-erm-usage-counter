package org.olf.erm.usage.counter41.csv.mapper;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.niso.schemas.counter.IdentifierType;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.csv.cellprocessor.IdentifierProcessor;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceProcessor;
import org.olf.erm.usage.counter41.csv.cellprocessor.ReportingPeriodProcessor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;

public class JR1 extends AbstractCSVMapper {

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
      "itemIdentifier",
      "itemIdentifier",
      "itemIdentifier",
      "itemIdentifier",
      "itemPerformance",
      "itemPerformance",
      "itemPerformance"
    };
  }

  @Override
  public String getTitle() {
    return "Journal Report 1 (R4)";
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
            new Optional(new IdentifierProcessor(IdentifierType.DOI)), // Journal DOI
            new Optional(
                new IdentifierProcessor(IdentifierType.PROPRIETARY)), // Proprietary Identifier
            new Optional(new IdentifierProcessor(IdentifierType.PRINT_ISSN)), // Print ISSN
            new Optional(new IdentifierProcessor(IdentifierType.ONLINE_ISSN)), // Online ISSN
            new Optional(
                new ReportingPeriodProcessor(MetricType.FT_TOTAL)), // Reporting Period Total
            new Optional(new ReportingPeriodProcessor(MetricType.FT_HTML)), // Reporting Period HTML
            new Optional(new ReportingPeriodProcessor(MetricType.FT_PDF)) // Reporting Period PDF
            );
    Stream<Optional> rest =
        getYearMonths().stream()
            .map(ym -> new Optional(new MonthPerformanceProcessor(ym, MetricType.FT_TOTAL)));
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }

  @Override
  public void writeItems(ICsvDozerBeanWriter writer) throws IOException {
    CellProcessor[] processors = createProcessors();
    for (final ReportItem item : getReport().getCustomer().get(0).getReportItems()) {
      writer.write(item, processors);
    }
  }

  @Override
  public String[] createTotals() {
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
        getYearMonths().stream()
            .map(ym -> bigIntToStringOrNull(getPeriodMetricTotal(MetricType.FT_TOTAL, ym)));
    return Stream.concat(first.stream(), rest).toArray(String[]::new);
  }
}
