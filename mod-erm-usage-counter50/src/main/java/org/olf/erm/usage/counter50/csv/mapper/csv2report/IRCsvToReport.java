package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseEnumType;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseItemAttributes;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseItemContributors;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseItemDates;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseItemIDs;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseMetricTypes;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParsePublisherID;
import org.olf.erm.usage.counter50.csv.mapper.MapperException;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.merger.ItemUsageMerger;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.merger.Merger;
import org.openapitools.client.model.COUNTERItemAttributes;
import org.openapitools.client.model.COUNTERItemContributors;
import org.openapitools.client.model.COUNTERItemContributors.TypeEnum;
import org.openapitools.client.model.COUNTERItemDates;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemParent;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERItemUsage;
import org.openapitools.client.model.COUNTERItemUsage.AccessMethodEnum;
import org.openapitools.client.model.COUNTERItemUsage.AccessTypeEnum;
import org.openapitools.client.model.COUNTERItemUsage.DataTypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class IRCsvToReport extends AbstractCsvToReport {

  private final ItemParser<COUNTERItemUsage> itemParser;
  private final Merger<COUNTERItemUsage> itemsMerger;

  public IRCsvToReport(String csvString) throws IOException, MapperException {
    super(csvString);
    this.itemParser = new ItemParser<>(COUNTERItemUsage.class);
    this.itemsMerger = new ItemUsageMerger();
  }

  @Override
  public COUNTERItemReport toReport() {
    COUNTERItemReport result = new COUNTERItemReport();
    SUSHIReportHeader sushiReportHeader = super.parseHeader();
    result.setReportHeader(sushiReportHeader);

    List<String> contentLines = lines.subList(CONTENT_START_LINE, lines.size());
    String titleUsagesString = String.join("\n", contentLines);
    List<YearMonth> yearMonths = Counter5Utils.getYearMonthsFromReportHeader(sushiReportHeader);

    List<COUNTERItemUsage> itemUsages =
        itemParser.parseItems(
            titleUsagesString,
            createFieldMapping(yearMonths),
            createProcessors(yearMonths),
            createHintTypes(yearMonths));
    itemUsages.forEach(
        ciu -> {
          if (ciu.getItemID() != null) {
            ciu.getItemID().removeIf(Objects::isNull);
            if (ciu.getItemID().isEmpty()) {
              ciu.setItemID(null);
            }
          }
          COUNTERItemParent itemParent = ciu.getItemParent();
          if (itemParent.getItemID() != null) {
            itemParent.getItemID().removeIf(Objects::isNull);
            if (itemParent.getItemID().isEmpty()) {
              itemParent.setItemID(null);
            }
          }
          boolean isAllParentAttributesNull =
              Stream.of(
                      itemParent.getItemID(),
                      itemParent.getItemAttributes(),
                      itemParent.getItemDates(),
                      itemParent.getDataType(),
                      itemParent.getItemContributors(),
                      itemParent.getItemName())
                  .allMatch(Objects::isNull);
          if (isAllParentAttributesNull) {
            ciu.setItemParent(null);
          }
        });

    result.setReportItems(itemsMerger.mergeItems(itemUsages));
    return result;
  }

  protected Class<?>[] createHintTypes(List<YearMonth> yearMonths) {
    Class<?>[] first = {
      null, // Title
      null, // Publisher
      null, // Publisher_ID
      null, // Platform
      COUNTERItemContributors.class, // Authors
      COUNTERItemDates.class, // Publication_Date
      COUNTERItemAttributes.class, // Article_Version
      COUNTERItemIdentifiers.class, // DOI
      COUNTERItemIdentifiers.class, // Proprietary_ID
      COUNTERItemIdentifiers.class, // ISBN
      COUNTERItemIdentifiers.class, // Print_ISSN
      COUNTERItemIdentifiers.class, // Online_ISSN
      COUNTERItemIdentifiers.class, // URI
      COUNTERItemParent.class, // Parent_Title
      COUNTERItemContributors.class, // Parent_Authors
      COUNTERItemDates.class, // Parent_Publication_Date
      COUNTERItemAttributes.class, // Parent_Article_Version
      COUNTERItemParent.DataTypeEnum.class, // Parent_Data_Type
      COUNTERItemIdentifiers.class, // Parent_DOI
      COUNTERItemIdentifiers.class, // Parent_Proprietary_ID
      COUNTERItemIdentifiers.class, // Parent_ISBN
      COUNTERItemIdentifiers.class, // Parent_Print_ISSN
      COUNTERItemIdentifiers.class, // Parent_Online_ISSN
      COUNTERItemIdentifiers.class, // Parent_URI
      //      COUNTERItemComponent.class, // Component_Title
      //      null, // Component_Authors
      //      null, // Component_Publication_Date
      //      null, // Component_Data_Type
      //      null, // Component_DOI
      //      null, // Component_Proprietary_DOI
      //      null, // Component_ISBN
      //      null, // Component_Print_ISSN
      //      null, // Component_Online_ISSN
      //      null, // Component_URI
      DataTypeEnum.class, // Data_Type
      null, // YOP
      AccessTypeEnum.class, // Access_Type
      AccessMethodEnum.class, // Access_Method
      null, // Metric_Type
      null // Reporting_Period_Total
    };
    Stream<Class<COUNTERItemPerformance>> rest =
        yearMonths.stream().map(ym -> COUNTERItemPerformance.class);
    return Stream.concat(Arrays.stream(first), rest).toArray(Class<?>[]::new);
  }

  public String[] createFieldMapping(List<YearMonth> yearMonths) {
    String[] y = yearMonths.stream().map(YearMonth::toString).toArray(String[]::new);
    String[] baseHeader =
        new String[] {
          "Item",
          "Publisher",
          "PublisherID",
          "Platform",
          "ItemContributors",
          "ItemDates",
          "ItemAttributes",
          "ItemID[0]",
          "ItemID[1]",
          "ItemID[2]",
          "ItemID[3]",
          "ItemID[4]",
          "ItemID[5]",
          "ItemParent[0].ItemName", // Parent_Title
          "ItemParent[0].ItemContributors", // Parent_Authors
          "ItemParent[0].ItemDates", // Parent_Publication_Date
          "ItemParent[0].ItemAttributes", // Parent_Article_Version
          "ItemParent[0].DataType", // Parent_Data_Type
          "ItemParent[0].ItemID[0]", // Parent_DOI
          "ItemParent[0].ItemID[1]", // Parent_Proprietary_ID
          "ItemParent[0].ItemID[2]", // Parent_ISBN
          "ItemParent[0].ItemID[3]", // Parent_Print_ISSN
          "ItemParent[0].ItemID[4]", // Parent_Online_ISSN
          "ItemParent[0].ItemID[5]", // Parent_URI
          //          "ItemComponent", // Component_Title
          //          null, // Component_Authors
          //          null, // Component_Publication_Date
          //          null, // Component_Data_Type
          //          null, // Component_DOI
          //          null, // Component_Proprietary_DOI
          //          null, // Component_ISBN
          //          null, // Component_Print_ISSN
          //          null, // Component_Online_ISSN
          //          null, // Component_URI
          "DataType",
          "YOP",
          "AccessType",
          "AccessMethod",
          null,
          null
        };
    for (int i = 0; i < yearMonths.size(); i++) {
      y[i] = "performance[" + i + "]";
    }
    return Stream.concat(Arrays.stream(baseHeader), Arrays.stream(y)).toArray(String[]::new);
  }

  protected CellProcessor[] createProcessors(List<YearMonth> yearMonths) {
    ParseMetricTypes parseMetricTypes = new ParseMetricTypes(getHeader(yearMonths));
    List<CellProcessorAdaptor> first =
        Arrays.asList(
            new Optional(), // Title
            new Optional(), // Publisher
            new Optional(new ParsePublisherID()), // Publisher_ID
            new Optional(), // Platform
            new Optional(new ParseItemContributors(TypeEnum.AUTHOR)), // Authors
            new Optional(new ParseItemDates(COUNTERItemDates.TypeEnum.PUBLICATION_DATE)),
            // Publication_Date
            new Optional(new ParseItemAttributes(COUNTERItemAttributes.TypeEnum.ARTICLE_VERSION)),
            // Article_Version
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.DOI)), // DOI
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.PROPRIETARY)),
            // Proprietary_ID
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.ISBN)), // ISBN
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN)),
            // Print_ISSN
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN)),
            // Online_ISSN
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.URI)), // URI
            new Optional(), // Parent_Title
            new Optional(new ParseItemContributors(TypeEnum.AUTHOR)), // Parent_Authors
            new Optional(
                new ParseItemDates(
                    COUNTERItemDates.TypeEnum.PUBLICATION_DATE)), // Parent_Publication_Date
            new Optional(
                new ParseItemAttributes(
                    COUNTERItemAttributes.TypeEnum.ARTICLE_VERSION)), // Parent_Article_Version
            new Optional(
                new ParseEnumType<>(COUNTERItemUsage.DataTypeEnum.class)), // Parent_Data_Type
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.DOI)), // Parent_DOI
            new Optional(
                new ParseItemIDs(
                    COUNTERItemIdentifiers.TypeEnum.PROPRIETARY)), // Parent_Proprietary_ID
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.ISBN)), // Parent_ISBN
            new Optional(
                new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN)), // Parent_Print_ISSN
            new Optional(
                new ParseItemIDs(
                    COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN)), // Parent_Online_ISSN
            new Optional(new ParseItemIDs(COUNTERItemIdentifiers.TypeEnum.URI)), // Parent_URI
            //            new Optional(new ParseItemComponent()), // Component_Title
            //            new Optional(), // Component_Authors
            //            new Optional(), // Component_Publication_Date
            //            new Optional(), // Component_Data_Type
            //            new Optional(), // Component_DOI
            //            new Optional(), // Component_Proprietary_DOI
            //            new Optional(), // Component_ISBN
            //            new Optional(), // Component_Print_ISSN
            //            new Optional(), // Component_Online_ISSN
            //            new Optional(), // Component_URI
            new Optional(new ParseEnumType<>(COUNTERItemUsage.DataTypeEnum.class)), // Data_Type
            new Optional(), // YOP
            new Optional(new ParseEnumType<>(COUNTERItemUsage.AccessTypeEnum.class)), // Access_Type
            new Optional(
                new ParseEnumType<>(COUNTERItemUsage.AccessMethodEnum.class)), // Access_Method
            new Optional(), // Metric_Type
            new Optional() // Reporting_Period_Total
            );

    List<ParseMetricTypes> metricTypeParsers =
        Collections.nCopies(yearMonths.size(), parseMetricTypes);
    return Stream.concat(first.stream(), metricTypeParsers.stream()).toArray(CellProcessor[]::new);
  }

  public String[] getHeader(List<YearMonth> yearMonths) {
    String[] y = yearMonths.stream().map(YearMonth::toString).toArray(String[]::new);
    String[] baseHeader =
        new String[] {
          "Item",
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
          //          "Component_Title",
          //          "Component_Authors",
          //          "Component_Publication_Date",
          //          "Component_Data_Type",
          //          "Component_DOI",
          //          "Component_Proprietary_DOI",
          //          "Component_ISBN",
          //          "Component_Print_ISSN",
          //          "Component_Online_ISSN",
          //          "Component_URI",
          "Data_Type",
          "YOP",
          "Access_Type",
          "Access_Method",
          "Metric_Type",
          "Reporting_Period_Total"
        };
    return Stream.concat(Arrays.stream(baseHeader), Arrays.stream(y)).toArray(String[]::new);
  }
}
