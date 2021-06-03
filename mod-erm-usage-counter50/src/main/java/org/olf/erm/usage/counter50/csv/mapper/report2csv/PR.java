package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.MetricTypeProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PerformanceProcessor;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class PR extends AbstractReportToCsvMapper<COUNTERPlatformReport> {

  private final COUNTERPlatformReport report;

  public PR(COUNTERPlatformReport report) {
    super(
        report.getReportHeader(),
        Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader()));
    this.report = report;
  }

  @Override
  protected String[] getHeader() {
    return new String[] {
      "Platform", "Data_Type", "Access_Method", "Metric_Type", "Reporting_Period_Total"
    };
  }

  @Override
  protected COUNTERPlatformReport getReport() {
    return report;
  }

  @Override
  protected List<Map<String, Object>> toMap(COUNTERPlatformReport report) {
    String[] header = getHeader();

    List<Map<String, Object>> result = new ArrayList<>();
    report
        .getReportItems()
        .forEach(
            reportItem -> {
              Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType =
                  MetricTypeProcessor.getPerformancesPerMetricType(reportItem.getPerformance());
              performancesPerMetricType
                  .keySet()
                  .forEach(
                      metricTypeEnum -> {
                        final Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put(header[0], reportItem.getPlatform());
                        itemMap.put(header[1], reportItem.getDataType());
                        itemMap.put(header[2], reportItem.getAccessMethod());
                        itemMap.put(header[3], metricTypeEnum);
                        itemMap.put(
                            header[4],
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

  @Override
  protected CellProcessor[] createProcessors() {
    List<Optional> first =
        Arrays.asList(
            new Optional(), // Platform
            new Optional(), // Data_Type
            new Optional(), // Access_Method
            new Optional(), // Metric_Type
            new Optional() // Reporting_Period_Total
            );
    Stream<Optional> rest = getYearMonths().stream().map(ym -> new Optional());
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }
}
