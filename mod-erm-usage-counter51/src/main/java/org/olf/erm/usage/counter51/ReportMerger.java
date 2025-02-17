package org.olf.erm.usage.counter51;

import static org.olf.erm.usage.counter51.JsonProperties.ATTRIBUTE_PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.BEGIN_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.CREATED;
import static org.olf.erm.usage.counter51.JsonProperties.CREATED_BY;
import static org.olf.erm.usage.counter51.JsonProperties.END_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.ITEMS;
import static org.olf.erm.usage.counter51.JsonProperties.PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ITEMS;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

class ReportMerger {

  public static final String MSG_PROPERTIES_DO_NOT_MATCH =
      REPORT_HEADER + " properties do not match";

  private ObjectNode removeHeaderProperties(ObjectNode header) {
    header.remove(CREATED);
    header.remove(CREATED_BY);
    ObjectNode reportFilters = header.withObject(REPORT_FILTERS);
    reportFilters.remove(BEGIN_DATE);
    reportFilters.remove(END_DATE);
    return header;
  }

  private ObjectNode deepCopyWithoutProperty(ObjectNode objectNode, String propertyName) {
    return objectNode.deepCopy().without(propertyName);
  }

  private Pair<String, String> combineDatePairs(
      Pair<String, String> pair1, Pair<String, String> pair2) {
    String minLeft =
        pair1.getLeft().compareTo(pair2.getLeft()) <= 0 ? pair1.getLeft() : pair2.getLeft();
    String maxRight =
        pair1.getRight().compareTo(pair2.getRight()) >= 0 ? pair1.getRight() : pair2.getRight();
    return Pair.of(minLeft, maxRight);
  }

  private Pair<String, String> getDatePairFromHeader(ObjectNode header) {
    ObjectNode reportFilters = header.withObject(REPORT_FILTERS);
    return Pair.of(reportFilters.get(BEGIN_DATE).asText(), reportFilters.get(END_DATE).asText());
  }

  private void validateReports(Collection<ObjectNode> reports) {
    List<ObjectNode> reportHeaders =
        reports.stream().map(on -> on.withObject(REPORT_HEADER)).map(ObjectNode::deepCopy).toList();

    boolean reportsHaveUniformHeaderProperties =
        reportHeaders.stream().map(this::removeHeaderProperties).distinct().count() == 1;
    if (!reportsHaveUniformHeaderProperties) {
      throw new IllegalArgumentException(MSG_PROPERTIES_DO_NOT_MATCH);
    }
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
   * @param reports the list of COUNTER reports to be merged.
   * @return a merged COUNTER report containing the combined data from all reports.
   * @throws MergerException if an error occurs during merging.
   */
  public ObjectNode mergeReports(List<ObjectNode> reports) {
    try {
      validateReports(reports);
      return reports.stream().map(ObjectNode::deepCopy).reduce(this::mergeReports).orElseThrow();
    } catch (Exception e) {
      throw new MergerException(e);
    }
  }

  private ObjectNode mergeReports(ObjectNode report1, ObjectNode report2) {
    ObjectNode mergedHeader =
        mergeHeaders(report1.withObject(REPORT_HEADER), report2.withObject(REPORT_HEADER));
    Collection<ObjectNode> mergedReportItems =
        mergeReportItemsCollection(
            Arrays.asList(report1.withArray(REPORT_ITEMS), report2.withArray(REPORT_ITEMS)));
    report1.set(REPORT_HEADER, mergedHeader);
    report1.withArray(REPORT_ITEMS).removeAll().addAll(mergedReportItems);
    return report1;
  }

  private Collection<ObjectNode> mergeReportItemsCollection(Collection<ArrayNode> reportItems) {
    boolean hasItems =
        reportItems.stream()
            .map(ArrayNode::elements)
            .flatMap(Streams::stream)
            .anyMatch(jn -> jn.has(ITEMS));

    return reportItems.stream()
        .map(ArrayNode::elements)
        .flatMap(Streams::stream)
        .map(jn -> (ObjectNode) jn)
        .collect(
            Collectors.toMap(
                on -> deepCopyWithoutProperty(on, hasItems ? ITEMS : ATTRIBUTE_PERFORMANCE),
                on -> on,
                (on1, on2) -> {
                  if (hasItems) {
                    List<ArrayNode> list =
                        Arrays.asList(on1.withArray(ITEMS), on2.withArray(ITEMS));
                    Collection<ObjectNode> nodes = mergeReportItemsCollection(list);
                    on1.withArray(ITEMS).removeAll().addAll(nodes);
                    return on1;
                  }
                  return mergeReportItems(on1, on2);
                }))
        .values();
  }

  private ObjectNode mergeReportItems(ObjectNode reportItem1, ObjectNode reportItem2) {
    List<ObjectNode> attributePerformances =
        Stream.of(
                reportItem1.withArray(ATTRIBUTE_PERFORMANCE),
                reportItem2.withArray(ATTRIBUTE_PERFORMANCE))
            .map(ArrayNode::elements)
            .flatMap(Streams::stream)
            .map(jn -> (ObjectNode) jn)
            .toList();
    reportItem1
        .withArray(ATTRIBUTE_PERFORMANCE)
        .removeAll()
        .addAll(mergeAttributePerformanceCollection(attributePerformances));
    return reportItem1;
  }

  private Collection<ObjectNode> mergeAttributePerformanceCollection(
      Collection<ObjectNode> collection) {
    return collection.stream()
        .collect(
            Collectors.toMap(
                on -> deepCopyWithoutProperty(on, PERFORMANCE),
                on -> on,
                this::mergeAttributePerformance))
        .values();
  }

  private ObjectNode mergeAttributePerformance(
      ObjectNode attributePerformance1, ObjectNode attributePerformance2) {
    attributePerformance1
        .withObject(PERFORMANCE)
        .setAll(
            mergePerformance(
                attributePerformance1.withObject(PERFORMANCE),
                attributePerformance2.withObject(PERFORMANCE)));
    return attributePerformance1;
  }

  private ObjectNode mergePerformance(ObjectNode performance1, ObjectNode performance2) {
    performance2
        .fields()
        .forEachRemaining(
            e -> performance1.withObject(e.getKey()).setAll((ObjectNode) e.getValue()));
    return performance1;
  }

  private ObjectNode mergeHeaders(ObjectNode header1, ObjectNode header2) {
    Pair<String, String> datePair =
        Stream.of(header1, header2)
            .map(this::getDatePairFromHeader)
            .reduce(this::combineDatePairs)
            .orElseThrow();

    header1
        .withObject(REPORT_FILTERS)
        .put(BEGIN_DATE, datePair.getLeft())
        .put(END_DATE, datePair.getRight());
    return header1;
  }

  static class MergerException extends RuntimeException {

    public static final String MSG_ERROR_MERGING_REPORT = "Error merging report: ";

    public MergerException(Throwable cause) {
      super(MSG_ERROR_MERGING_REPORT + cause.getMessage(), cause);
    }
  }
}
