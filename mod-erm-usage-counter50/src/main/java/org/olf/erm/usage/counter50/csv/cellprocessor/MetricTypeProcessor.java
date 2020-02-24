package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;

public final class MetricTypeProcessor {

  private MetricTypeProcessor() {}

  public static Map<MetricTypeEnum, Map<YearMonth, Integer>> getPerformancesPerMetricType(
      List<COUNTERItemPerformance> performances) {
    TreeMap<MetricTypeEnum, Map<YearMonth, Integer>> result = new TreeMap<>();
    List<MetricTypeEnum> metricTypes = getMetricTypes(performances);
    metricTypes.forEach(metricTypeEnum -> result.put(metricTypeEnum, new HashMap<>()));
    performances.stream()
        .forEach(
            p -> {
              YearMonth start =
                  YearMonth.parse(p.getPeriod().getBeginDate(), DateTimeFormatter.ISO_DATE);
              p.getInstance().stream()
                  .forEach(
                      i -> {
                        Map<YearMonth, Integer> currentMetricType = result.get(i.getMetricType());
                        currentMetricType.put(start, i.getCount());
                      });
            });
    return result;
  }

  public static List<MetricTypeEnum> getMetricTypes(List<COUNTERItemPerformance> performances) {
    return performances.stream()
        .flatMap(p -> p.getInstance().stream().map(COUNTERItemPerformanceInstance::getMetricType))
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }
}
