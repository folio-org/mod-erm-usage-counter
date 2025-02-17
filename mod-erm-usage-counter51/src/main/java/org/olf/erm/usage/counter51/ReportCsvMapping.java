package org.olf.erm.usage.counter51;

import static java.util.List.of;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.Delimiter.SEMICOLON_SPACE;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractAuthors;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractExceptions;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractIdentifiers;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractKeyValuePairs;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractReportingPeriod;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractValues;
import static org.olf.erm.usage.counter51.ReportCsvMappingItem.create;
import static org.olf.erm.usage.counter51.ReportType.DR;
import static org.olf.erm.usage.counter51.ReportType.DR_D1;
import static org.olf.erm.usage.counter51.ReportType.DR_D2;
import static org.olf.erm.usage.counter51.ReportType.IR;
import static org.olf.erm.usage.counter51.ReportType.IR_A1;
import static org.olf.erm.usage.counter51.ReportType.IR_M1;
import static org.olf.erm.usage.counter51.ReportType.PR;
import static org.olf.erm.usage.counter51.ReportType.PR_P1;
import static org.olf.erm.usage.counter51.ReportType.TR;
import static org.olf.erm.usage.counter51.ReportType.TR_B1;
import static org.olf.erm.usage.counter51.ReportType.TR_B2;
import static org.olf.erm.usage.counter51.ReportType.TR_B3;
import static org.olf.erm.usage.counter51.ReportType.TR_J1;
import static org.olf.erm.usage.counter51.ReportType.TR_J2;
import static org.olf.erm.usage.counter51.ReportType.TR_J3;
import static org.olf.erm.usage.counter51.ReportType.TR_J4;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("java:S1192") // String literals should not be duplicated
enum ReportCsvMapping {
  REPORT_HEADER(
      of(
          create("Report_Name"),
          create("Report_ID"),
          create("Release"),
          create("Institution_Name"),
          create("Institution_ID", n -> extractIdentifiers(n.path("Institution_ID"))),
          create(
              "Metric_Types",
              n -> extractValues(n.at("/Report_Filters/Metric_Type"), SEMICOLON_SPACE)),
          create(
              "Report_Filters",
              n ->
                  extractKeyValuePairs(
                      n.at("/Report_Filters"), of("Metric_Type", "Begin_Date", "End_Date"))),
          create("Report_Attributes", n -> extractKeyValuePairs(n.path("Report_Attributes"))),
          create("Exceptions", n -> extractExceptions(n.path("Exceptions"))),
          create("Reporting_Period", n -> extractReportingPeriod(n.path("Report_Filters"))),
          create("Created"),
          create("Created_By"),
          create("Registry_Record"))),

  REPORT_ITEM_DESCRIPTION(
      of(
          create("Database", of(DR, DR_D1, DR_D2)),
          create("Title", of(TR, TR_B1, TR_B2, TR_B3, TR_J1, TR_J2, TR_J3, TR_J4)),
          create("Item", of(IR, IR_A1, IR_M1)),
          create(
              "Publisher",
              of(
                  DR, TR, IR, DR_D1, DR_D2, TR_B1, TR_B2, TR_B3, TR_J1, TR_J2, TR_J3, TR_J4, IR_A1,
                  IR_M1)),
          create(
              "Publisher_ID",
              of(
                  DR, TR, IR, DR_D1, DR_D2, TR_B1, TR_B2, TR_B3, TR_J1, TR_J2, TR_J3, TR_J4, IR_A1,
                  IR_M1),
              n -> extractIdentifiers(n.path("Publisher_ID"))))),

  PLATFORM(of(create("Platform"))),

  REPORT_ITEM_IDENTIFIERS(
      of(
          create("Authors", of(IR, IR_A1), n -> extractAuthors(n.path("Authors"))),
          create("Publication_Date", of(IR, IR_A1)),
          create("Article_Version", of(IR, IR_A1)),
          create(
              "DOI",
              of(TR, IR, TR_B1, TR_B2, TR_B3, TR_J1, TR_J2, TR_J3, TR_J4, IR_A1, IR_M1),
              "/Item_ID/DOI"),
          create(
              "Proprietary_ID",
              of(
                  DR, TR, IR, DR_D1, DR_D2, TR_B1, TR_B2, TR_B3, TR_J1, TR_J2, TR_J3, TR_J4, IR_A1,
                  IR_M1),
              "/Item_ID/Proprietary"),
          create("ISBN", of(TR, IR, TR_B1, TR_B2, TR_B3), "/Item_ID/ISBN"),
          create(
              "Print_ISSN",
              of(TR, IR, TR_B1, TR_B2, TR_B3, TR_J1, TR_J2, TR_J3, TR_J4, IR_A1),
              "/Item_ID/Print_ISSN"),
          create(
              "Online_ISSN",
              of(TR, IR, TR_B1, TR_B2, TR_B3, TR_J1, TR_J2, TR_J3, TR_J4, IR_A1),
              "/Item_ID/Online_ISSN"),
          create(
              "URI",
              of(TR, IR, TR_B1, TR_B2, TR_B3, TR_J1, TR_J2, TR_J3, TR_J4, IR_A1, IR_M1),
              "/Item_ID/URI"))),
  PARENT_ITEM_DESCRIPTION(
      of(
          create("Parent_Title", of(IR, IR_A1), "Title"),
          create("Parent_Authors", of(IR, IR_A1), n -> extractAuthors(n.path("Authors"))),
          create("Parent_Publication_Date", of(IR), "Publication_Date"),
          create("Parent_Article_Version", of(IR, IR_A1), "Article_Version"),
          create("Parent_Data_Type", of(IR), "Data_Type"),
          create("Parent_DOI", of(IR, IR_A1), "/Item_ID/DOI"),
          create("Parent_Proprietary_ID", of(IR, IR_A1), "/Item_ID/Proprietary"),
          create("Parent_ISBN", of(IR), "/Item_ID/ISBN"),
          create("Parent_Print_ISSN", of(IR, IR_A1), "/Item_ID/Print_ISSN"),
          create("Parent_Online_ISSN", of(IR, IR_A1), "/Item_ID/Online_ISSN"),
          create("Parent_URI", of(IR, IR_A1), "/Item_ID/URI"))),

  REPORT_ATTRIBUTES(
      of(
          create("Data_Type", of(PR, DR, TR, IR, PR_P1, TR_B1, TR_B2, TR_B3, IR_M1)),
          create("YOP", of(TR, IR, TR_B1, TR_B2, TR_B3, TR_J4)),
          create("Access_Type", of(TR, IR, TR_B3, TR_J3, IR_A1)),
          create("Access_Method", of(PR, DR, TR, IR)))),

  METRIC_TYPES(of(create("Metric_Type")));

  private final List<ReportCsvMappingItem> reportCsvMappingItems;

  ReportCsvMapping(List<ReportCsvMappingItem> reportCsvMappingItems) {
    this.reportCsvMappingItems = reportCsvMappingItems;
  }

  public List<ReportCsvMappingItem> getMappingItems(ReportType reportType) {
    return reportCsvMappingItems.stream()
        .filter(reportCsvMappingItem -> reportCsvMappingItem.reports().contains(reportType))
        .toList();
  }

  public List<Function<JsonNode, String>> getMappingFunctions(ReportType reportType) {
    return reportCsvMappingItems.stream()
        .filter(reportCsvMappingItem -> reportCsvMappingItem.reports().contains(reportType))
        .map(ReportCsvMappingItem::mappingFunction)
        .toList();
  }

  public List<String> getMappingNames(ReportType reportType) {
    return reportCsvMappingItems.stream()
        .filter(reportCsvMappingItem -> reportCsvMappingItem.reports().contains(reportType))
        .map(ReportCsvMappingItem::name)
        .toList();
  }
}
