package org.olf.erm.usage.counter51;

import static org.olf.erm.usage.counter51.Counter51Utils.getYearMonths;
import static org.olf.erm.usage.counter51.JsonProperties.ATTRIBUTE_PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.BEGIN_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.END_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.ITEMS;
import static org.olf.erm.usage.counter51.JsonProperties.PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ITEMS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;
import java.time.YearMonth;
import java.util.List;

class ReportSplitter {

  private ObjectNode filterPerformance(ObjectNode performance, YearMonth yearMonth) {
    return performance.remove(
        Streams.stream(performance.fieldNames())
            .filter(s -> performance.withObject(s).retain(yearMonth.toString()).isEmpty())
            .toList());
  }

  private List<JsonNode> filterAttributePerformance(
      JsonNode attributePerformance, YearMonth yearMonth) {
    return Streams.stream(attributePerformance.elements())
        .filter(ap -> !filterPerformance(ap.withObject(PERFORMANCE), yearMonth).isEmpty())
        .toList();
  }

  private ArrayNode filterReportItems(ArrayNode reportItems, YearMonth yearMonth) {
    return Streams.stream(reportItems.elements())
        .filter(
            reportItem -> {
              if (reportItem.has(ITEMS)) { // Item report needs special handling
                return Streams.stream(reportItem.withArray(ITEMS).elements())
                    .anyMatch(
                        i ->
                            !filterAttributePerformance(
                                    i.withArray(ATTRIBUTE_PERFORMANCE), yearMonth)
                                .isEmpty());
              }
              return !filterAttributePerformance(
                      reportItem.withArray(ATTRIBUTE_PERFORMANCE), yearMonth)
                  .isEmpty();
            })
        .collect(JsonNodeFactory.instance::arrayNode, ArrayNode::add, ArrayNode::addAll);
  }

  private ObjectNode filterReportHeader(ObjectNode reportHeader, YearMonth yearMonth) {
    reportHeader
        .withObject(REPORT_FILTERS)
        .put(BEGIN_DATE, yearMonth.atDay(1).toString())
        .put(END_DATE, yearMonth.atEndOfMonth().toString());
    return reportHeader;
  }

  private ObjectNode filterReport(ObjectNode report, YearMonth yearMonth) {
    ObjectNode filteredReport = report.deepCopy();
    filteredReport.set(
        REPORT_HEADER, filterReportHeader(filteredReport.withObject(REPORT_HEADER), yearMonth));
    filteredReport.set(
        REPORT_ITEMS, filterReportItems(filteredReport.withArray(REPORT_ITEMS), yearMonth));
    return filteredReport;
  }

  /**
   * Splits a COUNTER report into multiple COUNTER reports that each span a single month.
   *
   * @param report the COUNTER report that should be splitted.
   * @return list of single-month COUNTER reports.
   * @throws SplitterException if an error occurs during splitting.
   */
  public List<ObjectNode> splitReport(ObjectNode report) {
    try {
      List<YearMonth> months = getYearMonths(report);
      return months.stream().map(yearMonth -> filterReport(report, yearMonth)).toList();
    } catch (Exception e) {
      throw new SplitterException(e);
    }
  }

  static class SplitterException extends RuntimeException {

    public static final String MSG_ERROR_SPLITTING_REPORT = "Error splitting report: ";

    public SplitterException(Throwable cause) {
      super(MSG_ERROR_SPLITTING_REPORT + cause.getMessage(), cause);
    }
  }
}
