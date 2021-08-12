package org.olf.erm.usage.counter50.converter.tr;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.olf.erm.usage.counter50.converter.ConverterUtils;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;
import org.openapitools.client.model.COUNTERTitleUsage.AccessMethodEnum;
import org.openapitools.client.model.COUNTERTitleUsage.DataTypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;

public class TRJ3Converter extends TRConverterBase {

  private static final List<MetricTypeEnum> metrics =
      List.of(
          MetricTypeEnum.TOTAL_ITEM_INVESTIGATIONS,
          MetricTypeEnum.TOTAL_ITEM_REQUESTS,
          MetricTypeEnum.UNIQUE_ITEM_INVESTIGATIONS,
          MetricTypeEnum.UNIQUE_ITEM_REQUESTS);

  private static final Predicate<COUNTERItemPerformanceInstance> hasMetricType =
      pi -> metrics.stream().anyMatch(m -> m.equals(pi.getMetricType()));

  private static final Predicate<COUNTERTitleUsage> filter =
      u ->
          DataTypeEnum.JOURNAL.equals(u.getDataType())
              && AccessMethodEnum.REGULAR.equals(u.getAccessMethod())
              && u.getPerformance().stream()
                  .anyMatch(p -> p.getInstance().stream().anyMatch(hasMetricType));

  private static final UnaryOperator<COUNTERTitleUsage> mapper =
      u -> {
        u.setDataType(null);
        u.setSectionType(null);
        u.setYOP(null);
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
          "Total_Item_Investigations|Total_Item_Requests|Unique_Item_Investigations|Unique_Item_Requests",
          "Data_Type",
          "Journal",
          "Access_Method",
          "Regular");

  @Override
  public Function<COUNTERTitleUsage, List<Object>> getGroupByAttributes() {
    return u ->
        List.of(
            u.getTitle(),
            u.getItemID(),
            u.getPlatform(),
            u.getPublisher(),
            u.getPublisherID(),
            u.getAccessType());
  }

  @Override
  public COUNTERTitleReport convert(COUNTERTitleReport report) {
    List<COUNTERTitleUsage> newReportItems =
        createNewReportItems(report.getReportItems(), filter, mapper);
    SUSHIReportHeader newReportHeader =
        ConverterUtils.createNewReportHeader(
            report.getReportHeader(), "TR_J3", "Journal Usage by Access Type", reportFiltersMap);
    return new COUNTERTitleReport().reportItems(newReportItems).reportHeader(newReportHeader);
  }
}
