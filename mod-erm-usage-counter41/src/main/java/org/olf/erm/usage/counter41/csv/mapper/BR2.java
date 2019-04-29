package org.olf.erm.usage.counter41.csv.mapper;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.csv.cellprocessor.IdentifierProcessor;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceProcessor;
import org.olf.erm.usage.counter41.csv.cellprocessor.ReportingPeriodProcessor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;

public class BR2 extends AbstractCSVMapper {

  public BR2(Report report) {
    super(report);
  }

  @Override
  public String[] getHeader() {
    return new String[] {
      "",
      "Publisher",
      "Platform",
      "Book DOI",
      "Proprietary Identifier",
      "ISBN",
      "ISSN",
      "Reporting Period Total"
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
      "itemPerformance"
    };
  }

  @Override
  public String getTitle() {
    return "Book Report 2 (R4)";
  }

  @Override
  public String getDescription() {
    return "Number of Successful Section Requests by Month and Title";
  }

  private CellProcessor[] createProcessors() {
    List<Optional> first =
        Arrays.asList(
            new Optional(), // Title
            new Optional(), // Publisher
            new Optional(), // Platform
            new Optional(new IdentifierProcessor("DOI")), // Book DOI
            new Optional(new IdentifierProcessor("Proprietary")), // Proprietary Identifier
            new Optional(new IdentifierProcessor("Print_ISBN")), // ISBN // FIXME: print or online?
            new Optional(new IdentifierProcessor("Print_ISSN")), // ISSN // FIXME: print or online?
            new Optional(
                new ReportingPeriodProcessor(MetricType.FT_TOTAL)) // Reporting Period Total
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
    String[] first =
        new String[] {
          "Total for all titles",
          Strings.emptyToNull(getSinglePublisher()),
          Strings.emptyToNull(getSinglePlatform()),
          null,
          null,
          null,
          null,
          bigIntToStringOrNull(getPeriodMetricTotal(MetricType.FT_TOTAL, null))
        };
    Stream<String> rest =
        getYearMonths().stream()
            .map(ym -> bigIntToStringOrNull(getPeriodMetricTotal(MetricType.FT_TOTAL, ym)));
    return Stream.concat(Arrays.stream(first), rest).toArray(String[]::new);
  }
}
