package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.MetricTypeProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PerformanceProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PublisherIDProcessor;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class DRD1 extends AbstractReportToCsvMapper<COUNTERDatabaseReport> {

  private final COUNTERDatabaseReport report;

  public DRD1(COUNTERDatabaseReport report) {
    super(
        report.getReportHeader(),
        Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader()));
    this.report = report;
  }

  @Override
  protected String[] getHeader() {
    return new String[] {
      "Database",
      "Publisher",
      "Publisher_ID",
      "Platform",
      "Proprietary_ID",
      "Metric_Type",
      "Reporting_Period_Total"
    };
  }

  @Override
  protected COUNTERDatabaseReport getReport() {
    return this.report;
  }

  @Override
  protected String getMetricTypes() {
    return report.getReportItems().stream()
        .flatMap(
            counterTitleUsage ->
                counterTitleUsage.getPerformance().stream()
                    .flatMap(
                        counterItemPerformance ->
                            counterItemPerformance.getInstance().stream()
                                .map(
                                    counterItemPerformanceInstance ->
                                        counterItemPerformanceInstance.getMetricType().getValue())))
        .distinct()
        .collect(Collectors.joining("; "));
  }

  @Override
  protected CellProcessor[] createProcessors() {
    List<Optional> first =
        Arrays.asList(
            new Optional(), // Database
            new Optional(), // Publisher
            new Optional(), // Publisher_ID
            new Optional(), // Platform
            new Optional(), // Proprietary_ID
            new Optional(), // Metric_Type
            new Optional() // Reporting_Period_Total
            );
    Stream<Optional> rest = getYearMonths().stream().map(ym -> new Optional());
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }

  @Override
  protected List<Map<String, Object>> toMap(COUNTERDatabaseReport report) {
    String[] header = getHeader();

    List<Map<String, Object>> result = new ArrayList<>();
    report
        .getReportItems()
        .forEach(
            dbReport -> {
              Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType =
                  MetricTypeProcessor.getPerformancesPerMetricType(dbReport.getPerformance());
              performancesPerMetricType
                  .keySet()
                  .forEach(
                      metricTypeEnum -> {
                        final Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put(header[0], dbReport.getDatabase());
                        itemMap.put(header[1], dbReport.getPublisher());
                        itemMap.put(
                            header[2],
                            PublisherIDProcessor.getPublisherID(dbReport.getPublisherID()));
                        itemMap.put(header[3], dbReport.getPlatform());
                        itemMap.put(header[4], ""); // TODO: What to put as Proprietary_ID?
                        itemMap.put(header[5], metricTypeEnum);
                        itemMap.put(
                            header[6],
                            PerformanceProcessor.calculateSum(
                                performancesPerMetricType, metricTypeEnum));

                        itemMap.putAll(
                            PerformanceProcessor.getPerformancePerMonth(
                                performancesPerMetricType,
                                metricTypeEnum,
                                getYearMonths(),
                                formatter));

                        result.add(itemMap);
                      });
            });
    return result;
  }
}
