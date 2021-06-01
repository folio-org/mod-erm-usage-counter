package org.olf.erm.usage.counter50.converter;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

public class ConverterUtils {

  private ConverterUtils() {}

  public static List<SUSHIReportHeaderReportFilters> createReportFiltersFromMap(
      Map<String, String> map) {
    return map.entrySet().stream()
        .map(e -> new SUSHIReportHeaderReportFilters().name(e.getKey()).value(e.getValue()))
        .collect(Collectors.toList());
  }

  public static int getMetricTypeCount(COUNTERItemPerformance p, MetricTypeEnum metricType) {
    return p.getInstance().stream()
        .filter(i -> i.getMetricType().equals(metricType))
        .findFirst()
        .map(COUNTERItemPerformanceInstance::getCount)
        .orElse(0);
  }

  public static List<COUNTERItemPerformance> sumCOUNTERItemPerformance(
      List<COUNTERItemPerformance> counterItemPerformanceList) {
    return counterItemPerformanceList.stream()
        .collect(
            Collectors.groupingBy(
                COUNTERItemPerformance::getPeriod,
                Collectors.reducing(
                    (p1, p2) -> {
                      COUNTERItemPerformance ciPerf = new COUNTERItemPerformance();

                      Arrays.stream(MetricTypeEnum.values())
                          .forEach(
                              type -> {
                                int count =
                                    getMetricTypeCount(p1, type) + getMetricTypeCount(p2, type);
                                if (count > 0) {
                                  ciPerf.addInstanceItem(
                                      new COUNTERItemPerformanceInstance()
                                          .metricType(type)
                                          .count(count));
                                }
                              });

                      if (!ciPerf.getInstance().isEmpty()) {
                        return ciPerf.period(p1.getPeriod());
                      } else {
                        return null;
                      }
                    })))
        .values()
        .stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  public static List<COUNTERItemPerformance> sumCOUNTERItemPerformance(
      List<COUNTERItemPerformance> counterItemPerformanceList1,
      List<COUNTERItemPerformance> counterItemPerformanceList2) {
    return sumCOUNTERItemPerformance(
        Stream.of(counterItemPerformanceList1, counterItemPerformanceList2)
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));
  }

  public static List<SUSHIReportHeaderReportFilters> mergeReportFilters(
      List<SUSHIReportHeaderReportFilters> filters1,
      List<SUSHIReportHeaderReportFilters> filters2) {
    return Stream.of(filters1, filters2)
        .flatMap(Collection::stream)
        .collect(
            Collectors.toMap(
                SUSHIReportHeaderReportFilters::getName,
                SUSHIReportHeaderReportFilters::getValue,
                (v1, v2) -> v1))
        .entrySet()
        .stream()
        .map(e -> new SUSHIReportHeaderReportFilters().name(e.getKey()).value(e.getValue()))
        .collect(Collectors.toList());
  }

  public static SUSHIReportHeader createNewReportHeader(
      SUSHIReportHeader reportHeader,
      String reportId,
      String reportName,
      Map<String, String> reportFilterMap) {

    return reportHeader
        .created(Instant.now().toString())
        .reportID(reportId)
        .reportName(reportName)
        .reportFilters(
            mergeReportFilters(
                createReportFiltersFromMap(reportFilterMap), reportHeader.getReportFilters()))
        .reportAttributes(Collections.emptyList());
  }
}
