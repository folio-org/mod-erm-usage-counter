package org.olf.erm.usage.counter50.converter.dr;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.olf.erm.usage.counter50.converter.ConverterUtils;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERDatabaseUsage;
import org.openapitools.client.model.COUNTERDatabaseUsage.AccessMethodEnum;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;

public class DRD1Converter extends DRConverterBase {
  private static final List<MetricTypeEnum> metrics =
      List.of(
          MetricTypeEnum.SEARCHES_AUTOMATED,
          MetricTypeEnum.SEARCHES_FEDERATED,
          MetricTypeEnum.SEARCHES_REGULAR,
          MetricTypeEnum.TOTAL_ITEM_INVESTIGATIONS,
          MetricTypeEnum.TOTAL_ITEM_REQUESTS);

  private static final Predicate<COUNTERItemPerformanceInstance> hasMetricType =
      pi -> metrics.stream().anyMatch(m -> m.equals(pi.getMetricType()));

  private static final Predicate<COUNTERDatabaseUsage> filter =
      u ->
          AccessMethodEnum.REGULAR.equals(u.getAccessMethod())
              && u.getPerformance().stream()
                  .anyMatch(p -> p.getInstance().stream().anyMatch(hasMetricType));

  private static final UnaryOperator<COUNTERDatabaseUsage> mapper =
      u -> {
        u.setDataType(null);
        u.setAccessMethod(null);
        u.getPerformance()
            .forEach(
                p -> {
                  List<COUNTERItemPerformanceInstance> newInstances =
                      p.getInstance().stream().filter(hasMetricType).collect(toList());
                  p.setInstance(newInstances);
                });
        return u;
      };

  private static final Map<String, String> reportFiltersMap =
      Map.of(
          "Metric_Type",
          "Searches_Automated|Searches_Federated|Searches_Regular|Total_Item_Investigations|"
              + "Total_Item_Requests",
          "Access_Method",
          "Regular");

  @Override
  public COUNTERDatabaseReport convert(COUNTERDatabaseReport report) {
    List<COUNTERDatabaseUsage> newReportItems =
        createNewReportItems(report.getReportItems(), filter, mapper);
    SUSHIReportHeader newReportHeader =
        ConverterUtils.createNewReportHeader(
            report.getReportHeader(), "DR_D1", "Database Search and Item Usage", reportFiltersMap);
    return new COUNTERDatabaseReport().reportItems(newReportItems).reportHeader(newReportHeader);
  }
}
