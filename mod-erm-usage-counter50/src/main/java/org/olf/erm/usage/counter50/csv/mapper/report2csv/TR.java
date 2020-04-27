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
import org.olf.erm.usage.counter50.csv.cellprocessor.IdentifierProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.MetricTypeProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PerformanceProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PublisherIDProcessor;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.COUNTERTitleReport;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class TR extends AbstractReportToCsvMapper<COUNTERTitleReport> {

  private final COUNTERTitleReport report;

  public TR(COUNTERTitleReport report) {
    super(
        report.getReportHeader(),
        Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader()));
    this.report = report;
  }

  @Override
  public String[] getHeader() {
    return new String[] {
      "Title",
      "Publisher",
      "Publisher_ID",
      "Platform",
      "DOI",
      "Proprietary_ID",
      "ISBN",
      "Print_ISSN",
      "Online_ISSN",
      "URI",
      "Data_Type",
      "Section_Type",
      "YOP",
      "Access_Type",
      "Access_Method",
      "Metric_Type",
      "Reporting_Period_Total"
    };
  }

  @Override
  protected COUNTERTitleReport getReport() {
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
            new Optional(), // Title
            new Optional(), // Publisher
            new Optional(), // Publisher_ID
            new Optional(), // Platform
            new Optional(), // DOI
            new Optional(), // Proprietary Identifier
            new Optional(), // ISBN
            new Optional(), // Print ISSN
            new Optional(), // Online ISSN
            new Optional(), // URI
            new Optional(), // Data_Type
            new Optional(), // Section_Type
            new Optional(), // YOP
            new Optional(), // Access_Type
            new Optional(), // Access_Method
            new Optional(), // Metric_Type
            new Optional() // Reporting_Period_Total
            );
    Stream<Optional> rest = getYearMonths().stream().map(ym -> new Optional());
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }

  @Override
  protected List<Map<String, Object>> toMap(COUNTERTitleReport report) {
    String[] header = getHeader();

    List<Map<String, Object>> result = new ArrayList<>();
    report.getReportItems().stream()
        .forEach(
            reportItem -> {
              Map<MetricTypeEnum, Map<YearMonth, Integer>> performancesPerMetricType =
                  MetricTypeProcessor.getPerformancesPerMetricType(reportItem.getPerformance());
              performancesPerMetricType
                  .keySet()
                  .forEach(
                      metricTypeEnum -> {
                        final Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put(header[0], reportItem.getTitle());
                        itemMap.put(header[1], reportItem.getPublisher());
                        itemMap.put(
                            header[2],
                            PublisherIDProcessor.getPublisherID(reportItem.getPublisherID()));
                        itemMap.put(header[3], reportItem.getPlatform());
                        itemMap.put(
                            header[4],
                            IdentifierProcessor.getValue(reportItem.getItemID(), TypeEnum.DOI));
                        itemMap.put(
                            header[5],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(), TypeEnum.PROPRIETARY));
                        itemMap.put(
                            header[6],
                            IdentifierProcessor.getValue(reportItem.getItemID(), TypeEnum.ISBN));
                        itemMap.put(
                            header[7],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(), TypeEnum.PRINT_ISSN));
                        itemMap.put(
                            header[8],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(), TypeEnum.ONLINE_ISSN));
                        itemMap.put(
                            header[9],
                            IdentifierProcessor.getValue(reportItem.getItemID(), TypeEnum.URI));
                        itemMap.put(header[10], reportItem.getDataType());
                        itemMap.put(header[11], reportItem.getSectionType());
                        itemMap.put(header[12], reportItem.getYOP());
                        itemMap.put(header[13], reportItem.getAccessType());
                        itemMap.put(header[14], reportItem.getAccessMethod());
                        itemMap.put(header[15], metricTypeEnum);

                        itemMap.put(
                            header[16],
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
