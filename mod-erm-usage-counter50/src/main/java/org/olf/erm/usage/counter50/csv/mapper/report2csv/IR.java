package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.IdentifierProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.MetricTypeProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PerformanceProcessor;
import org.olf.erm.usage.counter50.csv.cellprocessor.PublisherIDProcessor;
import org.openapitools.client.model.COUNTERItemAttributes;
import org.openapitools.client.model.COUNTERItemComponent;
import org.openapitools.client.model.COUNTERItemContributors;
import org.openapitools.client.model.COUNTERItemContributors.TypeEnum;
import org.openapitools.client.model.COUNTERItemDates;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemParent;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.COUNTERItemReport;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class IR extends AbstractReportToCsvMapper<COUNTERItemReport> {

  private final COUNTERItemReport report;

  public IR(COUNTERItemReport report) {
    super(
        report.getReportHeader(),
        Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader()));
    this.report = report;
  }

  @Override
  protected COUNTERItemReport getReport() {
    return this.report;
  }

  @Override
  public String[] getHeader() {
    return new String[] {
      "Title",
      "Publisher",
      "Publisher_ID",
      "Platform",
      "Authors",
      "Publication_Date",
      "Article_Version",
      "DOI",
      "Proprietary_ID",
      "ISBN",
      "Print_ISSN",
      "Online_ISSN",
      "URI",
      "Parent_Title",
      "Parent_Authors",
      "Parent_Publication_Date",
      "Parent_Article_Version",
      "Parent_Data_Type",
      "Parent_DOI",
      "Parent_Proprietary_ID",
      "Parent_ISBN",
      "Parent_Print_ISSN",
      "Parent_Online_ISSN",
      "Parent_URI",
      "Component_Title",
      "Component_Authors",
      "Component_Publication_Date",
      "Component_Data_Type",
      "Component_DOI",
      "Component_Proprietary_DOI",
      "Component_ISBN",
      "Component_Print_ISSN",
      "Component_Online_ISSN",
      "Component_URI",
      "Data_Type",
      "YOP",
      "Access_Type",
      "Access_Method",
      "Metric_Type",
      "Reporting_Period_Total"
    };
  }

  @Override
  protected CellProcessor[] createProcessors() {
    List<Optional> first =
        Arrays.asList(
            new Optional(), // Item
            new Optional(), // Publisher
            new Optional(), // Publisher_ID
            new Optional(), // Platform
            new Optional(), // Authors,
            new Optional(), // Publication_Date
            new Optional(), // Article_Version
            new Optional(), // DOI
            new Optional(), // Proprietary Identifier
            new Optional(), // ISBN
            new Optional(), // Print ISSN
            new Optional(), // Online ISSN
            new Optional(), // URI
            new Optional(), // Parent_Title
            new Optional(), // Parent_Authors
            new Optional(), // Parent_Publication_Date
            new Optional(), // Parent_Article_Version
            new Optional(), // Parent_Data_Type
            new Optional(), // Parent_DOI
            new Optional(), // Parent_Proprietary_ID
            new Optional(), // Parent_ISBN
            new Optional(), // Parent_Print_ISSN
            new Optional(), // Parent_Online_ISSN
            new Optional(), // Parent_URI
            new Optional(), // Component_Title
            new Optional(), // Component_Authors
            new Optional(), // Component_Publication_Date
            new Optional(), // Component_Data_Type
            new Optional(), // Component_DOI
            new Optional(), // Component_Proprietary_DOI
            new Optional(), // Component_ISBN
            new Optional(), // Component_Print_ISSN
            new Optional(), // Component_Online_ISSN
            new Optional(), // Component_URI
            new Optional(), // Data_Type
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
  protected List<Map<String, Object>> toMap(COUNTERItemReport report) {
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
                        itemMap.put(header[0], reportItem.getItem());
                        itemMap.put(header[1], reportItem.getPublisher());
                        itemMap.put(
                            header[2],
                            PublisherIDProcessor.getPublisherID(reportItem.getPublisherID()));
                        itemMap.put(header[3], reportItem.getPlatform());
                        itemMap.put(header[4], getAuthors(reportItem.getItemContributors()));
                        itemMap.put(header[5], getPublicationDate(reportItem.getItemDates()));
                        itemMap.put(header[6], getArticleVersion(reportItem.getItemAttributes()));
                        itemMap.put(
                            header[7],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(), COUNTERItemIdentifiers.TypeEnum.DOI));
                        itemMap.put(
                            header[8],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(),
                                COUNTERItemIdentifiers.TypeEnum.PROPRIETARY));
                        itemMap.put(
                            header[9],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(), COUNTERItemIdentifiers.TypeEnum.ISBN));
                        itemMap.put(
                            header[10],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(),
                                COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN));
                        itemMap.put(
                            header[11],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(),
                                COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN));
                        itemMap.put(
                            header[12],
                            IdentifierProcessor.getValue(
                                reportItem.getItemID(), COUNTERItemIdentifiers.TypeEnum.URI));
                        itemMap.put(header[13], getParentTitle(reportItem.getItemParent()));
                        itemMap.put(header[14], getParentAuthors(reportItem.getItemParent()));
                        itemMap.put(
                            header[15], getParentPublicationDate(reportItem.getItemParent()));
                        itemMap.put(
                            header[16], getParentArticleVersion(reportItem.getItemParent()));
                        itemMap.put(header[17], reportItem.getDataType());
                        itemMap.put(
                            header[18],
                            getParentIdentifier(
                                reportItem.getItemParent(), COUNTERItemIdentifiers.TypeEnum.DOI));
                        itemMap.put(
                            header[19],
                            getParentIdentifier(
                                reportItem.getItemParent(),
                                COUNTERItemIdentifiers.TypeEnum.PROPRIETARY));
                        itemMap.put(
                            header[20],
                            getParentIdentifier(
                                reportItem.getItemParent(), COUNTERItemIdentifiers.TypeEnum.ISBN));
                        itemMap.put(
                            header[21],
                            getParentIdentifier(
                                reportItem.getItemParent(),
                                COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN));
                        itemMap.put(
                            header[22],
                            getParentIdentifier(
                                reportItem.getItemParent(),
                                COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN));
                        itemMap.put(
                            header[23],
                            getParentIdentifier(
                                reportItem.getItemParent(), COUNTERItemIdentifiers.TypeEnum.URI));
                        itemMap.put(header[24], getComponentTitle(reportItem.getItemComponent()));
                        itemMap.put(header[25], getComponentAuthors(reportItem.getItemComponent()));
                        itemMap.put(
                            header[26], getComponentPublicationDate(reportItem.getItemComponent()));
                        itemMap.put(
                            header[27], getComponentDataType(reportItem.getItemComponent()));
                        itemMap.put(
                            header[28],
                            getComponentIdentifier(
                                reportItem.getItemComponent(),
                                COUNTERItemIdentifiers.TypeEnum.DOI));
                        itemMap.put(
                            header[29],
                            getComponentIdentifier(
                                reportItem.getItemComponent(),
                                COUNTERItemIdentifiers.TypeEnum.PROPRIETARY));
                        itemMap.put(
                            header[30],
                            getComponentIdentifier(
                                reportItem.getItemComponent(),
                                COUNTERItemIdentifiers.TypeEnum.ISBN));
                        itemMap.put(
                            header[31],
                            getComponentIdentifier(
                                reportItem.getItemComponent(),
                                COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN));
                        itemMap.put(
                            header[32],
                            getComponentIdentifier(
                                reportItem.getItemComponent(),
                                COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN));
                        itemMap.put(
                            header[33],
                            getComponentIdentifier(
                                reportItem.getItemComponent(),
                                COUNTERItemIdentifiers.TypeEnum.URI));
                        itemMap.put(header[34], reportItem.getDataType());
                        itemMap.put(header[35], reportItem.getYOP());
                        itemMap.put(header[36], reportItem.getAccessType());
                        itemMap.put(header[37], reportItem.getAccessMethod());
                        itemMap.put(header[38], metricTypeEnum);
                        itemMap.put(
                            header[39],
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

  private String getAuthors(List<COUNTERItemContributors> contributors) {
    return contributors.stream()
        .filter(c -> c.getType() == TypeEnum.AUTHOR)
        .map(COUNTERItemContributors::getName)
        .collect(Collectors.joining("; "));
  }

  private String getPublicationDate(List<COUNTERItemDates> dates) {
    return dates.stream()
        .filter(d -> d.getType() == COUNTERItemDates.TypeEnum.PUBLICATION_DATE)
        .map(COUNTERItemDates::getValue)
        .collect(Collectors.joining("; "));
  }

  private String getArticleVersion(List<COUNTERItemAttributes> attrs) {
    if (attrs == null) {
      return null;
    }
    return attrs.stream()
        .filter(Objects::nonNull)
        .filter(a -> a.getType() == COUNTERItemAttributes.TypeEnum.ARTICLE_VERSION)
        .map(COUNTERItemAttributes::getValue)
        .collect(Collectors.joining("; "));
  }

  private String getParentTitle(COUNTERItemParent parent) {
    return (parent == null) ? null : parent.getItemName();
  }

  private String getParentAuthors(COUNTERItemParent parent) {
    return (parent == null) ? null : getAuthors(parent.getItemContributors());
  }

  private String getParentPublicationDate(COUNTERItemParent parent) {
    return (parent == null) ? null : getPublicationDate(parent.getItemDates());
  }

  private String getParentArticleVersion(COUNTERItemParent parent) {
    return (parent == null) ? null : getArticleVersion(parent.getItemAttributes());
  }

  private String getParentIdentifier(
      COUNTERItemParent parent, COUNTERItemIdentifiers.TypeEnum identifier) {
    return (parent == null) ? null : IdentifierProcessor.getValue(parent.getItemID(), identifier);
  }

  private String getComponentTitle(List<COUNTERItemComponent> components) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(COUNTERItemComponent::getItemName)
        .collect(Collectors.joining("; "));
  }

  private String getComponentAuthors(List<COUNTERItemComponent> components) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(c -> getAuthors(c.getItemContributors()))
        .collect(Collectors.joining("; "));
  }

  private String getComponentPublicationDate(List<COUNTERItemComponent> components) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(c -> getPublicationDate(c.getItemDates()))
        .collect(Collectors.joining("; "));
  }

  private String getComponentDataType(List<COUNTERItemComponent> components) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(c -> c.getDataType().getValue())
        .collect(Collectors.joining("; "));
  }

  private String getComponentIdentifier(
      List<COUNTERItemComponent> components, COUNTERItemIdentifiers.TypeEnum identifier) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(c -> IdentifierProcessor.getValue(c.getItemID(), identifier))
        .collect(Collectors.joining(", "));
  }
}
