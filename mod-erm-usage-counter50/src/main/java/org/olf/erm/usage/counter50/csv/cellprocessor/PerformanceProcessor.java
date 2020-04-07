package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;

public final class PerformanceProcessor {

  private PerformanceProcessor() {
  }

  public static int calculateSum(
      Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType,
      MetricTypeEnum metricTypeEnum) {
    return performancesPerMetricType.get(metricTypeEnum).values().stream()
        .mapToInt(x -> x == null ? 0 : x).sum();
  }

  public static Map<String, Integer> getPerformancePerMonth(
      Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType,
      MetricTypeEnum metricTypeEnum,
      List<YearMonth> yearMonths,
      DateTimeFormatter formatter) {

    Map<String, Integer> collected = new HashMap<>();
    yearMonths.forEach(yearMonth -> collected
        .put(yearMonth.format(formatter), performancesPerMetricType.get(metricTypeEnum)
            .getOrDefault(yearMonth, null)));
    return collected;
  }
}
