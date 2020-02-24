package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;

public final class PerformanceProcessor {

  private PerformanceProcessor() {}

  public static int calculateSum(
      Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType,
      MetricTypeEnum metricTypeEnum) {
    return performancesPerMetricType.get(metricTypeEnum).values().stream().mapToInt(x -> x).sum();
  }

  public static Map<String, Integer> getPerformancePerMonth(
      Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType,
      MetricTypeEnum metricTypeEnum,
      List<YearMonth> yearMonths,
      DateTimeFormatter formatter) {

    return yearMonths.stream()
        .collect(
            Collectors.toMap(
                yearMonth -> yearMonth.format(formatter),
                yearMonth -> performancesPerMetricType.get(metricTypeEnum).get(yearMonth)));
  }
}
