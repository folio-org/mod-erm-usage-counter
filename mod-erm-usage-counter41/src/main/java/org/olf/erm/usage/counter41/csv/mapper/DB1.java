package org.olf.erm.usage.counter41.csv.mapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Stream;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceProcessor;
import org.olf.erm.usage.counter41.csv.cellprocessor.ReportingPeriodProcessor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

public class DB1 extends AbstractCounterReport {

  public DB1(Report report) {
    super(report);
  }

  @Override
  public String[] getHeader() {
    return new String[] {
      "Database", "Publisher", "Platform", "User Activity", "Reporting Period Total"
    };
  }

  @Override
  public String[] getFieldMapping() {
    return new String[] {
      "itemName", "itemPublisher", "itemPlatform", "itemPlatform", "itemPerformance"
    };
  }

  @Override
  public String getTitle() {
    return "Database Report 1 (R4)";
  }

  @Override
  public String getDescription() {
    return "Total Searches, Result Clicks and Record Views by Month and Database";
  }

  enum Activity {
    SEARCH_REG("Regular Searches", MetricType.SEARCH_REG),
    SEARCH_FED("Searches-federated and automated", MetricType.SEARCH_FED),
    RESULT_CLICK("Result Clicks", MetricType.RESULT_CLICK),
    RECORD_VIEW("Record Views", MetricType.RECORD_VIEW);

    private String text;
    private MetricType metricType;

    Activity(String text, MetricType metricType) {
      this.text = text;
      this.metricType = metricType;
    }

    public String getText() {
      return text;
    }

    public MetricType getMetricType() {
      return metricType;
    }
  }

  private CellProcessor[] getProcessors(Activity activity) {

    CellProcessor[] first =
        new CellProcessor[] {
          new Optional(), // Database
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

      // write items
      for (final ReportItem item : report.getCustomer().get(0).getReportItems()) {
        for (Activity a : Activity.values()) {
          beanWriter.write(item, getProcessors(a));
        }
      }

      beanWriter.flush();
      return stringWriter.toString();
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }
}
