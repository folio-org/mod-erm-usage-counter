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
  private static final ObjectMapper objectMapper = createDefaultObjectMapper();
  private static final ReportConverter reportConverter = new ReportConverter(objectMapper);
  private static final ReportCsvConverter reportCsvConverter = new ReportCsvConverter(objectMapper);

  private Counter51Utils() {}

  /**
   * Converts a given master report into a standard view.
   *
   * @param report The original master report represented as an ObjectNode. This is the JSON
   *     structure that needs to be converted.
   * @param reportType The type of report to convert to. This determines the structure and
   *     attributes of the resulting report.
   * @return An ObjectNode representing the converted report.
   * @throws ReportConverterException if the target report type is not a standard view or if the
   *     master report is considered invalid according to the target report type.
   */
  public static ObjectNode convertReport(ObjectNode report, ReportType reportType) {
    return reportConverter.convert(report, reportType);
  }

  public static void writeReportAsCsv(JsonNode report, Appendable writer) throws IOException {
    reportCsvConverter.convert(report, writer, CSVFormat.DEFAULT);
  }

  /**
   * Creates a {@link ObjectMapper} instance that is configured with validation support for COUNTER
   * 5.1 report models.
   *
   * <p>See {@link ObjectMapperFactory#createDefault()}.
   *
   * @return a configured {@link ObjectMapper} instance.
   */
  public static ObjectMapper createDefaultObjectMapper() {
    return ObjectMapperFactory.createDefault();
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
