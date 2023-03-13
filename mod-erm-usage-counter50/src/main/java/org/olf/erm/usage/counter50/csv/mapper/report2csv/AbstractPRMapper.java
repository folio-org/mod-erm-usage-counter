package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.MetricTypeProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PerformanceProcessor;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERPlatformUsage;

abstract class AbstractPRMapper extends AbstractReportToCsvMapper<COUNTERPlatformReport> {

  AbstractPRMapper(COUNTERPlatformReport report) {
    super(
        report.getReportHeader(),
        Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader()));
    this.report = report;
  }

  abstract List<Object> getValues(COUNTERPlatformUsage platformUsage);

  @Override
  Stream<Map<String, Object>> toMapStream(COUNTERPlatformReport report) {
    String[] header = getHeader();
    return report.getReportItems().stream()
        .flatMap(
            platformUsage -> {
              Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType =
                  MetricTypeProcessor.getPerformancesPerMetricType(platformUsage.getPerformance());
              return performancesPerMetricType.keySet().stream()
                  .map(
                      metricTypeEnum -> {
                        List<Object> values = getValues(platformUsage);
                        Map<String, Object> itemMap =
                            IntStream.range(0, values.size())
                                .boxed()
                                .collect(
                                    LinkedHashMap::new,
                                    (m, i) -> m.put(header[i], values.get(i)),
                                    LinkedHashMap::putAll);
                        itemMap.put("Metric_Type", metricTypeEnum);
                        itemMap.put(
                            "Reporting_Period_Total",
                            PerformanceProcessor.calculateSum(
                                performancesPerMetricType, metricTypeEnum));

                        itemMap.putAll(
                            PerformanceProcessor.getPerformancePerMonth(
                                performancesPerMetricType, metricTypeEnum, yearMonths, formatter));

                        return itemMap;
                      });
            });
  }
}
