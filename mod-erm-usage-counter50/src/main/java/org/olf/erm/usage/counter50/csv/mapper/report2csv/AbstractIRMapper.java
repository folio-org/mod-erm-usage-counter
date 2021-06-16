package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.MetricTypeProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PerformanceProcessor;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERItemUsage;

public abstract class AbstractIRMapper extends AbstractReportToCsvMapper<COUNTERItemReport> {

  protected AbstractIRMapper(COUNTERItemReport report) {
    super(
        report.getReportHeader(),
        Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader()));
    this.report = report;
  }

  protected abstract List<Object> getValues(COUNTERItemUsage itemUsage);

  @Override
  protected List<Map<String, Object>> toMap(COUNTERItemReport report) {
    List<String> header = Arrays.asList(getHeader());

    List<Map<String, Object>> result = new ArrayList<>();
    report
        .getReportItems()
        .forEach(
            itemUsage -> {
              Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType =
                  MetricTypeProcessor.getPerformancesPerMetricType(itemUsage.getPerformance());
              performancesPerMetricType
                  .keySet()
                  .forEach(
                      metricTypeEnum -> {
                        List<Object> values = getValues(itemUsage);
                        Map<String, Object> itemMap =
                            IntStream.range(0, values.size())
                                .boxed()
                                .collect(
                                    LinkedHashMap::new,
                                    (m, i) -> m.put(header.get(i), values.get(i)),
                                    LinkedHashMap::putAll);
                        itemMap.put("Metric_Type", metricTypeEnum);
                        itemMap.put(
                            "Reporting_Period_Total",
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
