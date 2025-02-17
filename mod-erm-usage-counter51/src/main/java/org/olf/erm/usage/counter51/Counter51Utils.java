package org.olf.erm.usage.counter51;

import static org.olf.erm.usage.counter51.JsonProperties.BEGIN_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.END_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.olf.erm.usage.counter51.ReportConverter.ReportConverterException;
import org.olf.erm.usage.counter51.ReportMerger.MergerException;
import org.olf.erm.usage.counter51.ReportSplitter.SplitterException;

public class Counter51Utils {

  private static final ReportSplitter reportSplitter = new ReportSplitter();
  private static final ReportMerger reportMerger = new ReportMerger();
  private static final ObjectMapper objectMapper = ObjectMapperFactory.createDefault();
  private static final ReportConverter reportConverter = new ReportConverter(objectMapper);
  private static final ReportCsvConverter reportCsvConverter = new ReportCsvConverter(objectMapper);

  private Counter51Utils() {}

  /**
   * Converts a given COUNTER 5.1 Master Report into a specified Standard View.
   *
   * <p>This method leverages the {@link ReportConverter} to transform the JSON structure of the
   * master report into the desired report type, ensuring that the resulting report adheres to the
   * specified format and attributes.
   *
   * @param report The original master report represented as an {@link ObjectNode}. This is the JSON
   *     structure that needs to be converted.
   * @param reportType The type of report to convert to. This determines the structure and
   *     attributes of the resulting report.
   * @return An {@link ObjectNode} representing the converted report.
   * @throws ReportConverterException if the target report type is not a standard view or if the
   *     master report is considered invalid according to the target report type.
   */
  public static ObjectNode convertReport(ObjectNode report, ReportType reportType) {
    return reportConverter.convert(report, reportType);
  }

  /**
   * Converts a given COUNTER 5.1 report represented as a {@link JsonNode} into CSV format and
   * writes it to the provided {@link Appendable} writer.
   *
   * <p>This method utilizes the {@link ReportCsvConverter} to handle the conversion process,
   * ensuring that the report is valid.
   *
   * @param report The COUNTER report to be converted, represented as a {@link JsonNode}.
   * @param writer The {@link Appendable} writer where the CSV output will be written.
   * @throws IOException if an I/O error occurs during writing to the provided writer.
   */
  public static void writeReportAsCsv(JsonNode report, Appendable writer) throws IOException {
    reportCsvConverter.convert(report, writer, CSVFormat.DEFAULT);
  }

  /**
   * Converts a given COUNTER 5.1 report object into CSV format and writes it to the provided {@link
   * Appendable} writer.
   *
   * <p>This method first converts the report object into a {@link JsonNode} using the default
   * {@link ObjectMapper}, and then delegates the conversion to CSV format to {@link
   * #writeReportAsCsv(JsonNode, Appendable)}.
   *
   * @param report The COUNTER report object to be converted. This object is transformed into a
   *     {@link JsonNode} for further processing.
   * @param writer The {@link Appendable} writer where the CSV output will be written. This writer
   *     receives the CSV representation of the report.
   * @throws IOException if an I/O error occurs during writing to the provided writer.
   */
  public static void writeReportAsCsv(Object report, Appendable writer) throws IOException {
    JsonNode jsonNode = objectMapper.valueToTree(report);
    writeReportAsCsv(jsonNode, writer);
  }

  /**
   * Retrieves the default {@link ObjectMapper} instance used for handling JSON operations within
   * the COUNTER 5.1 report models.
   *
   * <p>This instance is pre-configured with necessary settings to ensure compatibility and
   * validation support for the specific JSON structures used in COUNTER 5.1 reports.
   *
   * @return the default {@link ObjectMapper} instance.
   */
  public static ObjectMapper getDefaultObjectMapper() {
    return objectMapper;
  }

  /**
   * Splits a COUNTER report into multiple COUNTER reports that each span a single month.
   *
   * <p>See {@link ReportSplitter#splitReport(ObjectNode)}
   *
   * @param report the COUNTER report that should be splitted.
   * @return list of single-month COUNTER reports.
   * @throws SplitterException if an error occurs during splitting.
   */
  public static List<ObjectNode> splitReport(ObjectNode report) {
    return reportSplitter.splitReport(report);
  }

  /**
   * Merges a list of COUNTER reports into a single merged COUNTER report where the merged COUNTER
   * report contains the combined data of all reports. Provided reports need to
   *
   * <ul>
   *   <li>span a single month only
   *   <li>have equal <em>Report_Header</em> attributes, except for <em>Created</em>,
   *       <em>Created_By</em>, <em>Report_Filters.Begin_Date</em> and
   *       <em>Report_Filters.End_Date</em>
   * </ul>
   *
   * See {@link ReportMerger#mergeReports(List)}
   *
   * @param reports the list of COUNTER reports to be merged.
   * @return a merged COUNTER report containing the combined data from all reports.
   * @throws MergerException if an error occurs during merging.
   */
  public static ObjectNode mergeReports(List<ObjectNode> reports) {
    return reportMerger.mergeReports(reports);
  }

  private static List<YearMonth> getYearMonths(YearMonth begin, YearMonth end) {
    return Stream.iterate(begin, next -> next.plusMonths(1))
        .limit(begin.until(end, ChronoUnit.MONTHS) + 1)
        .toList();
  }

  /**
   * Retrieves a list of {@link YearMonth} objects representing all the months between the start and
   * end dates found in the specified {@link ObjectNode}.
   *
   * <p>The method expects the JSON structure to contain a {@link JsonProperties#REPORT_HEADER}
   * object, which in turn contains {@link JsonProperties#REPORT_FILTERS} with {@link
   * JsonProperties#BEGIN_DATE} and {@link JsonProperties#END_DATE} fields. These dates are parsed
   * as {@link LocalDate} and converted into {@link YearMonth}.
   *
   * <p>The dates are used to create a list of {@link YearMonth} objects, which span from the start
   * month to the end month, inclusive.
   *
   * @param objectNode the {@link ObjectNode} that contains the JSON structure with report header,
   *     filters, and date fields
   * @return a list of {@link YearMonth} objects from the begin date to the end date
   * @throws NullPointerException if the expected fields (report header, report filters, begin date,
   *     or end date) are missing
   * @throws DateTimeParseException if the date strings cannot be parsed into {@link LocalDate}
   */
  public static List<YearMonth> getYearMonths(ObjectNode objectNode) {
    JsonNode reportHeader = objectNode.get(REPORT_HEADER);
    JsonNode reportFilters = reportHeader.get(REPORT_FILTERS);
    String beginDate = reportFilters.get(BEGIN_DATE).asText();
    String endDate = reportFilters.get(END_DATE).asText();
    YearMonth beginMonth = YearMonth.from(LocalDate.parse(beginDate));
    YearMonth endMonth = YearMonth.from(LocalDate.parse(endDate));
    return getYearMonths(beginMonth, endMonth);
  }

  /**
   * Gets the report type of the given COUNTER report.
   *
   * @param reportNode COUNTER report JSON node including Report_Header properties.
   * @return the report type as {@link ReportType}
   */
  public static ReportType getReportType(JsonNode reportNode) {
    return ReportType.valueOf(reportNode.at("/Report_Header/Report_ID").asText());
  }
}
