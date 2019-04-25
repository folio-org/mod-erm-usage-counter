package org.olf.erm.usage.counter41.csv.mapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceProcessor;
import org.olf.erm.usage.counter41.csv.cellprocessor.ReportingPeriodProcessor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;
import org.supercsv.util.CsvContext;

public class PR1 extends AbstractCounterReport {

  public PR1(Report report) {
    super(report);
  }

  @Override
  public String[] getHeader() {
    return new String[] {"Platform", "Publisher", "User Activity", "Reporting Period Total"};
  }

  @Override
  public String[] getFieldMapping() {
    return new String[] {"itemPlatform", "itemPublisher", "itemPublisher", "itemPerformance"};
  }

  @Override
  public String getTitle() {
    return "Platform Report 1 (R4)";
  }

  @Override
  public String getDescription() {
    return "Total Searches, Result Clicks and Record Views by Month and Platform";
  }

  private CellProcessor[] getProcessors(Activity activity) {
    CellProcessor[] first =
        new CellProcessor[] {
          new Optional(), // Publisher
          new Optional(), // Platform
          new CellProcessor() {
            @Override
            public String execute(Object value, CsvContext context) {
              return activity.getText();
            }
          }, // User Activity
          new ReportingPeriodProcessor(activity.getMetricType()) // Reporting Period Total
        };
    Stream<Optional> rest =
        yearMonths.stream()
            .map(ym -> new Optional(new MonthPerformanceProcessor(ym, activity.getMetricType())));
    return Stream.concat(Arrays.stream(first), rest).toArray(CellProcessor[]::new);
  }

  @Override
  public void writeItems(ICsvDozerBeanWriter writer) throws IOException {
    for (final ReportItem item : report.getCustomer().get(0).getReportItems()) {
      for (Activity a : Activity.values()) {
        writer.write(item, getProcessors(a));
      }
    }
  }
}
