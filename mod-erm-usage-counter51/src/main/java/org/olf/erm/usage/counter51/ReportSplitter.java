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

/**
 * Splits a COUNTER 5.1 report into one report per month covered by its header.
 *
 * <p>Usage is held per month, keyed by the month itself, so a month's report is built from the
 * entries of that month alone.
 */
class ReportSplitter {

  private static final String MSG_EXPECTED_ARRAY = "Expected %s to be an array but was %s";
  private static final String MSG_EXPECTED_OBJECT = "Expected %s to be an object but was %s";

  /**
   * Splits a COUNTER report into multiple COUNTER reports that each span a single month.
   *
   * @param report the COUNTER report that should be splitted.
   * @return list of single-month COUNTER reports.
   * @throws SplitterException if the report is not shaped the way the COUNTER 5.1 format
   *     prescribes, or if an error occurs during splitting.
   */
  public List<ObjectNode> splitReport(ObjectNode report) {
    try {
      validateShapeOf(report);
      return getYearMonths(report).stream()
          .map(yearMonth -> createReportForMonth(report, yearMonth))
          .toList();
    } catch (SplitterException e) {
      throw e; // already states why the report was rejected
    } catch (Exception e) {
      throw new SplitterException(e);
    }
  }

  /**
   * Fails the split if the report items are not shaped the way the COUNTER 5.1 format prescribes.
   * The methods below read the report leniently, so without this a malformed report would be split
   * into empty or garbled monthly reports rather than be rejected.
   *
   * <p>An absent property is not malformed: an absent Report_Items or Performance is carried over
   * as an empty one. Neither is a Performance or a metric that is {@code null}, which states an
   * absence of usage.
   *
   * <p>Rejects a report with a {@link SplitterException} of its own rather than leaving it to the
   * catch-all of {@link #splitReport}, which states no more than that something went wrong.
   */
  private void validateShapeOf(ObjectNode report) {
    if (report.has(REPORT_ITEMS)) {
      validateReportItems(report.get(REPORT_ITEMS), REPORT_ITEMS);
    }
  }

  /** Validates the report items of a report, or the items an item report nests below one. */
  private void validateReportItems(JsonNode reportItems, String propertyName) {
    requireArray(reportItems, propertyName);
    reportItems.forEach(reportItem -> validateReportItem(reportItem, entryOf(propertyName)));
  }

  private void validateReportItem(JsonNode reportItem, String description) {
    requireObject(reportItem, description);
    if (reportItem.has(ITEMS)) { // an item report nests its usage one level deeper
      validateReportItems(reportItem.get(ITEMS), ITEMS);
    }
    if (reportItem.has(ATTRIBUTE_PERFORMANCE)) {
      validateAttributePerformances(reportItem.get(ATTRIBUTE_PERFORMANCE));
    }
  }

  private void validateAttributePerformances(JsonNode attributePerformances) {
    requireArray(attributePerformances, ATTRIBUTE_PERFORMANCE);
    attributePerformances.forEach(
        attributePerformance -> {
          requireObject(attributePerformance, entryOf(ATTRIBUTE_PERFORMANCE));
          validatePerformance(attributePerformance.path(PERFORMANCE));
        });
  }

  private void validatePerformance(JsonNode performance) {
    if (performance.isMissingNode() || performance.isNull()) {
      return;
    }
    requireObject(performance, PERFORMANCE);
    performance
        .properties()
        .forEach(
            metric -> {
              if (!metric.getValue().isNull()) {
                requireObject(metric.getValue(), PERFORMANCE + "." + metric.getKey());
              }
            });
  }

  private String entryOf(String propertyName) {
    return "an entry of " + propertyName;
  }

  private void requireArray(JsonNode node, String description) {
    if (!node.isArray()) {
      throw new SplitterException(MSG_EXPECTED_ARRAY.formatted(description, node.getNodeType()));
    }
  }

  private void requireObject(JsonNode node, String description) {
    if (!node.isObject()) {
      throw new SplitterException(MSG_EXPECTED_OBJECT.formatted(description, node.getNodeType()));
    }
  }

  /** Creates the report holding the usage of a single month. */
  private ObjectNode createReportForMonth(ObjectNode report, YearMonth yearMonth) {
    ObjectNode reportForMonth = JsonNodeFactory.instance.objectNode();
    report
        .properties()
        .forEach(
            property ->
                reportForMonth.set(
                    property.getKey(),
                    createReportPropertyForMonth(
                        property.getKey(), property.getValue(), yearMonth)));
    if (!reportForMonth.has(REPORT_ITEMS)) { // a report without any report items still gets one
      reportForMonth.putArray(REPORT_ITEMS);
    }
    return reportForMonth;
  }

  /** Everything but the header and the report items is carried over unchanged. */
  private JsonNode createReportPropertyForMonth(
      String propertyName, JsonNode property, YearMonth yearMonth) {
    if (REPORT_HEADER.equals(propertyName)) {
      return createReportHeaderForMonth(property, yearMonth);
    }
    if (REPORT_ITEMS.equals(propertyName)) {
      return createReportItemsForMonth(property, yearMonth.toString());
    }
    return shallowCopyOf(property);
  }

  /** Carries the header over with its Begin_Date and End_Date narrowed to the given month. */
  private ObjectNode createReportHeaderForMonth(JsonNode reportHeader, YearMonth yearMonth) {
    ObjectNode reportHeaderForMonth = shallowCopyOfObject(reportHeader);
    ObjectNode reportFiltersForMonth = shallowCopyOfObject(reportHeader.path(REPORT_FILTERS));
    reportFiltersForMonth
        .put(BEGIN_DATE, yearMonth.atDay(1).toString())
        .put(END_DATE, yearMonth.atEndOfMonth().toString());
    reportHeaderForMonth.set(REPORT_FILTERS, reportFiltersForMonth);
    return reportHeaderForMonth;
  }

  /** Rebuilds every report item holding usage for the month, in the order of the source report. */
  private ArrayNode createReportItemsForMonth(JsonNode reportItems, String month) {
    return Streams.stream(reportItems.elements())
        .filter(reportItem -> holdsUsageOfMonth(reportItem, month))
        .map(reportItem -> createReportItemForMonth(reportItem, month))
        .collect(JsonNodeFactory.instance::arrayNode, ArrayNode::add, ArrayNode::addAll);
  }

  /**
   * Rebuilds a report item, carrying over the usage of the given month only. Also used for the
   * items an item report nests below its report items, which are shaped the same way.
   */
  private ObjectNode createReportItemForMonth(JsonNode reportItem, String month) {
    ObjectNode reportItemForMonth = JsonNodeFactory.instance.objectNode();
    reportItem
        .properties()
        .forEach(
            property ->
                reportItemForMonth.set(
                    property.getKey(),
                    createPropertyForMonth(property.getKey(), property.getValue(), month)));
    return reportItemForMonth;
  }

  /** Everything but the usage is carried over unchanged. */
  private JsonNode createPropertyForMonth(String propertyName, JsonNode property, String month) {
    if (ATTRIBUTE_PERFORMANCE.equals(propertyName)) {
      return createAttributePerformancesForMonth(property, month);
    }
    if (ITEMS.equals(propertyName)) { // an item report nests its usage one level deeper
      return createItemsForMonth(property, month);
    }
    return shallowCopyOf(property);
  }

  private ArrayNode createItemsForMonth(JsonNode items, String month) {
    ArrayNode itemsForMonth = JsonNodeFactory.instance.arrayNode();
    items.forEach(item -> itemsForMonth.add(createReportItemForMonth(item, month)));
    return itemsForMonth;
  }

  /**
   * Every attribute performance is kept, including one left without usage for the month, which then
   * holds an empty Performance.
   */
  private ArrayNode createAttributePerformancesForMonth(
      JsonNode attributePerformances, String month) {
    ArrayNode attributePerformancesForMonth = JsonNodeFactory.instance.arrayNode();
    attributePerformances.forEach(
        attributePerformance ->
            attributePerformancesForMonth.add(
                createAttributePerformanceForMonth(attributePerformance, month)));
    return attributePerformancesForMonth;
  }

  private ObjectNode createAttributePerformanceForMonth(
      JsonNode attributePerformance, String month) {
    ObjectNode attributePerformanceForMonth = JsonNodeFactory.instance.objectNode();
    attributePerformance
        .properties()
        .forEach(
            property ->
                attributePerformanceForMonth.set(
                    property.getKey(),
                    PERFORMANCE.equals(property.getKey())
                        ? createPerformanceForMonth(property.getValue(), month)
                        : shallowCopyOf(property.getValue())));
    if (!attributePerformanceForMonth.has(PERFORMANCE)) {
      attributePerformanceForMonth.putObject(PERFORMANCE);
    }
    return attributePerformanceForMonth;
  }

  /** Keeps the metrics holding usage for the given month, each reduced to that month. */
  private ObjectNode createPerformanceForMonth(JsonNode performance, String month) {
    ObjectNode performanceForMonth = JsonNodeFactory.instance.objectNode();
    performance
        .properties()
        .forEach(
            metric -> {
              JsonNode usageOfMonth = metric.getValue().path(month);
              if (!usageOfMonth.isMissingNode()) {
                performanceForMonth.putObject(metric.getKey()).set(month, usageOfMonth);
              }
            });
    return performanceForMonth;
  }

  /**
   * Returns a copy of the given node that shares its content, the counterpart of copying a list
   * without copying its elements: what is added to or removed from the copy cannot be seen by the
   * source, but what is modified inside it can. Value nodes are immutable and are returned as they
   * are.
   */
  private JsonNode shallowCopyOf(JsonNode node) {
    if (node.isObject()) {
      return shallowCopyOfObject(node);
    }
    if (node.isArray()) {
      ArrayNode copy = JsonNodeFactory.instance.arrayNode();
      copy.addAll((ArrayNode) node);
      return copy;
    }
    return node;
  }

  /** Returns a shallow copy of the given node, or an empty object if it is not one. */
  private ObjectNode shallowCopyOfObject(JsonNode node) {
    ObjectNode copy = JsonNodeFactory.instance.objectNode();
    if (node.isObject()) {
      copy.setAll((ObjectNode) node);
    }
    return copy;
  }

  /**
   * Tests whether the report item holds usage for the given month. Written with plain loops because
   * it runs once per report item and month.
   */
  private boolean holdsUsageOfMonth(JsonNode reportItem, String month) {
    if (reportItem.has(ITEMS)) { // an item report nests its usage one level deeper
      for (JsonNode item : reportItem.path(ITEMS)) {
        if (holdsUsageOfMonth(item, month)) {
          return true;
        }
      }
      return false;
    }
    for (JsonNode attributePerformance : reportItem.path(ATTRIBUTE_PERFORMANCE)) {
      for (JsonNode metric : attributePerformance.path(PERFORMANCE)) {
        if (metric.has(month)) {
          return true;
        }
      }
    }
    return false;
  }

  static class SplitterException extends RuntimeException {

    public static final String MSG_ERROR_SPLITTING_REPORT = "Error splitting report: ";

    public SplitterException(Throwable cause) {
      super(MSG_ERROR_SPLITTING_REPORT + cause.getMessage(), cause);
    }

    /** Used to reject a report that {@code splitReport} cannot make sense of. */
    public SplitterException(String message) {
      super(MSG_ERROR_SPLITTING_REPORT + message);
    }
  }
}
