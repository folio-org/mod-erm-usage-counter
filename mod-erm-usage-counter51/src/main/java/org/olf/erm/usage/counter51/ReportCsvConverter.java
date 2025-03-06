package org.olf.erm.usage.counter51;

import static java.util.Collections.emptyList;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractMetricTypes;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractPlatform;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractUsageData;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractValuesViaMapping;
import static org.olf.erm.usage.counter51.ReportCsvMapping.METRIC_TYPES;
import static org.olf.erm.usage.counter51.ReportCsvMapping.PARENT_ITEM_DESCRIPTION;
import static org.olf.erm.usage.counter51.ReportCsvMapping.PLATFORM;
import static org.olf.erm.usage.counter51.ReportCsvMapping.REPORT_ATTRIBUTES;
import static org.olf.erm.usage.counter51.ReportCsvMapping.REPORT_HEADER;
import static org.olf.erm.usage.counter51.ReportCsvMapping.REPORT_ITEM_DESCRIPTION;
import static org.olf.erm.usage.counter51.ReportCsvMapping.REPORT_ITEM_IDENTIFIERS;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.olf.erm.usage.counter51.ReportValidator.ReportValidatorException;
import org.olf.erm.usage.counter51.ReportValidator.ValidationResult;

class ReportCsvConverter {

  private static final DateTimeFormatter monthYearFormatter =
      DateTimeFormatter.ofPattern("MMM-yyyy");
  private static final DateTimeFormatter yearMonthFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM");
  private final ReportValidator reportValidator;

  public ReportCsvConverter(ObjectMapper objectMapper) {
    this.reportValidator = new ReportValidator(objectMapper);
  }

  public void convert(JsonNode reportNode, Appendable appendable, CSVFormat format)
      throws IOException, ReportValidatorException {
    ValidationResult validationResult = reportValidator.validateReport(reportNode);
    if (!validationResult.isValid()) {
      throw new ReportValidatorException(validationResult.getErrorMessage());
    }
    try (CSVPrinter printer = new CSVPrinter(appendable, format)) {
      printReport(printer, reportNode);
    }
  }

  private void printReport(CSVPrinter printer, JsonNode reportNode) throws IOException {
    ReportType reportType = Counter51Utils.getReportType(reportNode);
    List<YearMonth> months = Counter51Utils.getYearMonths((ObjectNode) reportNode);
    printReportHeader(printer, reportNode, reportType);
    printColumnHeadings(printer, reportType, months);
    printReportBody(printer, reportNode, reportType, months);
    printer.flush();
  }

  private void printColumnHeadings(
      CSVPrinter printer, ReportType reportType, List<YearMonth> months) throws IOException {
    List<String> attributes =
        Stream.of(
                REPORT_ITEM_DESCRIPTION,
                PLATFORM,
                REPORT_ITEM_IDENTIFIERS,
                PARENT_ITEM_DESCRIPTION,
                REPORT_ATTRIBUTES,
                METRIC_TYPES)
            .map(mapping -> mapping.getMappingNames(reportType))
            .flatMap(Collection::stream)
            .toList();
    List<String> usageMonths = getFormattedMonths(months, monthYearFormatter);
    List<String> columnHeadings =
        Stream.of(attributes, List.of("Reporting_Period_Total"), usageMonths)
            .flatMap(Collection::stream)
            .toList();
    printer.printRecord(columnHeadings);
  }

  private void printReportHeader(CSVPrinter printer, JsonNode reportNode, ReportType reportType)
      throws IOException {
    JsonPointer jsonPointer = JsonPointer.valueOf("/Report_Header");

    for (ReportCsvMappingItem mi : REPORT_HEADER.getMappingItems(reportType)) {
      String name = mi.name();
      String value = mi.mappingFunction().apply(reportNode.at(jsonPointer));
      printer.printRecord(value.isEmpty() ? List.of(name) : List.of(name, value));
    }
    printer.printRecord();
  }

  private void printReportBody(
      CSVPrinter printer, JsonNode reportNode, ReportType reportType, List<YearMonth> months)
      throws IOException {
    List<String> usageMonths = getFormattedMonths(months, yearMonthFormatter);

    for (JsonNode reportItem : reportNode.path("Report_Items")) {
      if (reportType.isItemReport()) {
        List<String> parentItemDescription =
            extractValuesViaMapping(reportItem, reportType, PARENT_ITEM_DESCRIPTION);

        for (JsonNode item : reportItem.at("/Items")) {
          processItems(printer, item, parentItemDescription, reportType, usageMonths);
        }
      } else {
        processItems(printer, reportItem, emptyList(), reportType, usageMonths);
      }
    }
  }

  private void processItems(
      CSVPrinter printer,
      JsonNode itemNode,
      List<String> parentItemDescription,
      ReportType reportType,
      List<String> months)
      throws IOException {
    List<String> reportItemDescriptions =
        extractValuesViaMapping(itemNode, reportType, REPORT_ITEM_DESCRIPTION);
    String platform = extractPlatform(itemNode);
    List<String> reportItemIdentifiers =
        extractValuesViaMapping(itemNode, reportType, REPORT_ITEM_IDENTIFIERS);

    for (JsonNode attributePerformance : itemNode.path("Attribute_Performance")) {
      List<String> reportAttributes =
          extractValuesViaMapping(attributePerformance, reportType, REPORT_ATTRIBUTES);
      JsonNode performance = attributePerformance.path("Performance");

      for (String metricType : extractMetricTypes(performance)) {
        List<String> usageData = extractUsageData(performance.path(metricType), months);
        printer.printRecord(
            createBodyRow(
                reportItemDescriptions,
                platform,
                reportItemIdentifiers,
                parentItemDescription,
                reportAttributes,
                metricType,
                usageData));
      }
    }
  }

  private List<String> getFormattedMonths(List<YearMonth> months, DateTimeFormatter formatter) {
    return months.stream().map(formatter::format).toList();
  }

  private List<String> createBodyRow(
      List<String> reportItemDescriptions,
      String platform,
      List<String> reportItemIdentifiers,
      List<String> parentItemDescription,
      List<String> reportAttributes,
      String metricType,
      List<String> usageData) {
    return Stream.of(
            reportItemDescriptions,
            List.of(platform),
            reportItemIdentifiers,
            parentItemDescription,
            reportAttributes,
            List.of(metricType),
            usageData)
        .flatMap(Collection::stream)
        .toList();
  }
}
