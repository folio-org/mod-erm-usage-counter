package org.olf.erm.usage.counter51;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.Reader;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.olf.erm.usage.counter51.ReportValidator.ReportValidatorException;
import org.olf.erm.usage.counter51.ReportValidator.ValidationResult;

/**
 * A converter for transforming COUNTER 5.1 reports from CSV format to JSON representation. This
 * class parses CSV data, processes and validates the structure according to COUNTER 5.1
 * specifications, and outputs a structured JSON representation that conforms to the expected report
 * format.
 */
class ReportJsonConverter {

  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM-yyyy");
  private static final int REPORT_HEADER_END_INDEX = 13;
  private static final int REPORT_ID_COLUMN_INDEX = 1;
  private static final int REPORT_ID_ROW_INDEX = 1;
  private static final int DATA_HEADER_ROW_INDEX = 14;
  private static final int DATA_ROWS_START_INDEX = 15;
  static final String REPORTING_PERIOD_TOTAL = "Reporting_Period_Total";
  private final ObjectMapper objectMapper;
  private final ReportValidator reportValidator;

  public ReportJsonConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.reportValidator = new ReportValidator(objectMapper);
  }

  /**
   * Converts a COUNTER 5.1 report from CSV into JSON representation.
   *
   * <p>This method takes a reader containing CSV data and a CSV format specification, parses the
   * CSV into records, and then processes those records into a structured JSON representation
   * according to the COUNTER 5.1 report format.
   *
   * <p>Currently, JSON mappings are available for COUNTER 5.1 master reports (TR, DR, IR, PR).
   *
   * @param reader the Reader containing CSV data to convert
   * @param csvFormat the CSVFormat defining the parsing rules for the CSV input
   * @return a JsonNode containing the structured report data
   * @throws IOException if an error occurs while reading from the provided reader
   * @throws ReportProcessingException if an error occurs during report processing
   * @throws ReportValidatorException if the generated report fails validation
   * @throws UnknownReportException if the report type cannot be determined
   * @throws UnsupportedReportException if the report type is not supported
   */
  public JsonNode convert(Reader reader, CSVFormat csvFormat) throws IOException {
    List<List<String>> allRecords = parseCSV(reader, csvFormat);
    return processRecords(allRecords);
  }

  private String getReportId(List<List<String>> allRecords) {
    try {
      return allRecords.get(REPORT_ID_ROW_INDEX).get(REPORT_ID_COLUMN_INDEX);
    } catch (Exception e) {
      throw new ReportProcessingException("Error while extracting Report_ID", e);
    }
  }

  private ObjectNode processRecords(List<List<String>> allRecords)
      throws ReportProcessingException,
          ReportValidatorException,
          UnknownReportException,
          UnsupportedReportException {
    // Extract report ID and determine type and mapping
    String reportId = getReportId(allRecords);
    ReportType reportType = getReportType(reportId);
    ReportJsonMapping reportJsonMapping = getReportMapping(reportId);

    ObjectNode resultNode;
    try {
      // Split data into sections
      List<List<String>> headerRows = extractHeaderRows(allRecords);
      List<String> dataHeader = extractDataHeader(allRecords);
      List<List<String>> dataRecords = extractDataRecords(allRecords);

      // Process data sections
      Map<List<String>, Object> groupedRecords =
          groupByColumns(dataRecords, reportJsonMapping.getColumnGroups());
      List<YearMonth> yearMonths = getYearMonthsFromHeaderRow(dataHeader);

      // Build JSON structure
      ObjectNode headerNode =
          buildHeaderJsonStructure(headerRows, reportJsonMapping.getHeaderMapping());
      ObjectNode itemsNode =
          buildItemsJsonStructure(
              groupedRecords, reportJsonMapping.getColumnGroupMappings(), yearMonths);

      resultNode = mergeObjectNodes(headerNode, itemsNode);
    } catch (Exception e) {
      throw new ReportProcessingException("Error while processing report data", e);
    }

    // Validate resulting report
    ValidationResult validationResult = reportValidator.validateReport(resultNode, reportType);
    if (!validationResult.isValid()) {
      throw new ReportValidator.ReportValidatorException(validationResult.getErrorMessage());
    }
    return resultNode;
  }

  private List<List<String>> parseCSV(Reader reader, CSVFormat csvFormat) throws IOException {
    try (CSVParser parser = csvFormat.parse(reader)) {
      return parser.stream().map(CSVRecord::toList).toList();
    }
  }

  private ReportType getReportType(String reportId) {
    try {
      return ReportType.valueOf(reportId);
    } catch (IllegalArgumentException e) {
      throw new UnknownReportException(
          "Could not determine report type for reportId '" + reportId + "'.");
    }
  }

  private ReportJsonMapping getReportMapping(String reportId) {
    try {
      return ReportJsonMapping.valueOf(reportId);
    } catch (IllegalArgumentException e) {
      throw new UnsupportedReportException("'" + reportId + "' is not a supported report type.");
    }
  }

  private List<List<String>> extractHeaderRows(List<List<String>> allRecords) {
    return allRecords.subList(0, REPORT_HEADER_END_INDEX + 1);
  }

  private List<String> extractDataHeader(List<List<String>> allRecords) {
    return allRecords.get(DATA_HEADER_ROW_INDEX);
  }

  private List<List<String>> extractDataRecords(List<List<String>> allRecords) {
    return allRecords.subList(DATA_ROWS_START_INDEX, allRecords.size());
  }

  private List<YearMonth> getYearMonthsFromHeaderRow(List<String> headerRow) {
    int reportingPeriodTotalIndex = headerRow.indexOf(REPORTING_PERIOD_TOTAL);
    return headerRow.subList(reportingPeriodTotalIndex + 1, headerRow.size()).stream()
        .map(DATE_TIME_FORMATTER::parse)
        .map(YearMonth::from)
        .toList();
  }

  @SuppressWarnings("unchecked")
  private Map<List<String>, Object> groupByColumns(
      List<List<String>> data, List<int[]> columnGroups) {
    return (Map<List<String>, Object>) recursiveGroupBy(data, columnGroups, 0);
  }

  private Object recursiveGroupBy(List<List<String>> data, List<int[]> columnGroups, int depth) {
    if (depth == columnGroups.size()) {
      // Base case: return the list of ungrouped columns
      List<Integer> columnsToRemove =
          columnGroups.stream().flatMap(arr -> Arrays.stream(arr).boxed()).toList();
      return data.stream()
          .map(row -> removeColumns(row, columnsToRemove))
          .collect(Collectors.toList());
    }

    int[] columnsToGroupBy = columnGroups.get(depth);
    return data.stream()
        .collect(
            Collectors.groupingBy(
                row -> Arrays.stream(columnsToGroupBy).mapToObj(row::get).toList(),
                LinkedHashMap::new,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    groupedRows -> recursiveGroupBy(groupedRows, columnGroups, depth + 1))));
  }

  private List<String> removeColumns(List<String> row, List<Integer> columnsToRemove) {
    return IntStream.range(0, row.size())
        .filter(i -> !columnsToRemove.contains(i))
        .mapToObj(row::get)
        .toList();
  }

  private ObjectNode mergeObjectNodes(ObjectNode... nodes) {
    return Arrays.stream(nodes)
        .collect(objectMapper::createObjectNode, ObjectNode::setAll, ObjectNode::setAll);
  }

  private ObjectNode buildHeaderJsonStructure(
      List<List<String>> headerRows, ReportJsonMappingItem headerMapping) {
    List<String> headerData =
        headerRows.stream().map(row -> row.size() > 1 ? row.get(1) : "").toList();

    return objectMapper
        .createObjectNode()
        .set(headerMapping.getPropertyName(), headerMapping.applyMapping(headerData));
  }

  private ObjectNode buildItemsJsonStructure(
      Map<List<String>, Object> groupedRecords,
      List<ReportJsonMappingItem> mappings,
      List<YearMonth> yearMonths) {
    ArrayNode itemsArray = processEntries(groupedRecords, mappings, yearMonths, 0);
    return objectMapper.createObjectNode().set(mappings.get(0).getPropertyName(), itemsArray);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private ArrayNode processEntries(
      Map<List<String>, Object> groupedRecords,
      List<ReportJsonMappingItem> mappings,
      List<YearMonth> yearMonths,
      int depth) {
    ArrayNode arrayNode = objectMapper.createArrayNode();
    groupedRecords.forEach(
        (k, v) -> {
          // Apply mapping function for this level
          ObjectNode mappedNode = (ObjectNode) mappings.get(depth).applyMapping(k);

          // Process nested content
          if (v instanceof Map map) {
            JsonNode nestedContent = processEntries(map, mappings, yearMonths, depth + 1);
            mappedNode.set(mappings.get(depth + 1).getPropertyName(), nestedContent);
          } else if (v instanceof List<?> list) {
            List<List<String>> typedList = (List<List<String>>) list;
            JsonNode valueNode =
                mappings.get(depth + 1).applyMapping(new Object[] {typedList, yearMonths});
            mappedNode.set(mappings.get(depth + 1).getPropertyName(), valueNode);
          }

          arrayNode.add(mappedNode);
        });

    return arrayNode;
  }

  static class UnknownReportException extends RuntimeException {
    UnknownReportException(String message) {
      super(message);
    }
  }

  static class UnsupportedReportException extends RuntimeException {
    UnsupportedReportException(String message) {
      super(message);
    }
  }

  static class ReportProcessingException extends RuntimeException {

    public ReportProcessingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
