package org.olf.erm.usage.counter51;

import static org.olf.erm.usage.counter51.Counter51Utils.getDefaultObjectMapper;
import static org.olf.erm.usage.counter51.Delimiter.SEMICOLON_SPACE;
import static org.olf.erm.usage.counter51.IdentifierNamespaces.INSTITUTION_IDENTIFIERS;
import static org.olf.erm.usage.counter51.JsonProperties.CREATED;
import static org.olf.erm.usage.counter51.JsonProperties.CREATED_BY;
import static org.olf.erm.usage.counter51.JsonProperties.EXCEPTIONS;
import static org.olf.erm.usage.counter51.JsonProperties.INSTITUTION_ID;
import static org.olf.erm.usage.counter51.JsonProperties.INSTITUTION_NAME;
import static org.olf.erm.usage.counter51.JsonProperties.PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.REGISTRY_RECORD;
import static org.olf.erm.usage.counter51.JsonProperties.RELEASE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ATTRIBUTES;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ID;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_NAME;
import static org.olf.erm.usage.counter51.ReportFieldProcessor.createExceptions;
import static org.olf.erm.usage.counter51.ReportFieldProcessor.createIdentifiers;
import static org.olf.erm.usage.counter51.ReportFieldProcessor.createReportAttributes;
import static org.olf.erm.usage.counter51.ReportFieldProcessor.createReportFilters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.YearMonth;
import java.util.List;

/**
 * Represents a mapping item that defines how to transform parts of a COUNTER 5.1 report from CSV to
 * JSON format. Each mapping item contains a property name and a function that transforms the input
 * data to the appropriate JSON structure.
 *
 * <p>This class provides standard mapping implementations for common report elements like report
 * headers and performance data, as well as support for custom mapping functions.
 */
class ReportJsonMappingItem {

  private final MappingFunction mappingFunction;
  private final String propertyName;

  @FunctionalInterface
  public interface MappingFunction {
    JsonNode apply(Object data);
  }

  // Private constructor
  private ReportJsonMappingItem(String propertyName, MappingFunction mappingFunction) {
    this.propertyName = propertyName;
    this.mappingFunction = mappingFunction;
  }

  public static ReportJsonMappingItem of(String propertyName, MappingFunction mappingFunction) {
    return new ReportJsonMappingItem(propertyName, mappingFunction);
  }

  public static ReportJsonMappingItem defaultPerformanceMappingItem() {
    return ReportJsonMappingItem.of(PERFORMANCE, ReportJsonMappingItem::mapPerformance);
  }

  public static ReportJsonMappingItem defaultReportHeaderMappingItem() {
    return ReportJsonMappingItem.of(REPORT_HEADER, ReportJsonMappingItem::mapReportHeader);
  }

  public String getPropertyName() {
    return propertyName;
  }

  public JsonNode applyMapping(Object data) {
    return mappingFunction.apply(data);
  }

  @SuppressWarnings("unchecked")
  private static ObjectNode mapPerformance(Object data) {
    Object[] args = (Object[]) data;
    List<List<String>> performanceData = (List<List<String>>) args[0];
    List<YearMonth> yearMonths = (List<YearMonth>) args[1];
    return performanceData.stream()
        .map(row -> mapPerformanceMetric(yearMonths, row))
        .filter(node -> !node.isEmpty())
        .collect(
            getDefaultObjectMapper()::createObjectNode, ObjectNode::setAll, ObjectNode::setAll);
  }

  private static ObjectNode mapPerformanceMetric(List<YearMonth> yearMonths, List<String> row) {
    NodeBuilder monthNodeBuilder = new NodeBuilder();
    for (int i = 0; i < yearMonths.size(); i++) {
      monthNodeBuilder.putOptional(yearMonths.get(i).toString(), Integer.parseInt(row.get(i + 2)));
    }
    return new NodeBuilder().putOptional(row.get(0), monthNodeBuilder.build()).build();
  }

  @SuppressWarnings("unchecked")
  private static ObjectNode mapReportHeader(Object data) {
    List<String> list = (List<String>) data;
    return new NodeBuilder()
        .putRequired(RELEASE, list.get(2))
        .putRequired(REPORT_ID, list.get(1))
        .putRequired(REPORT_NAME, list.get(0))
        .putRequired(CREATED, list.get(10))
        .putRequired(CREATED_BY, list.get(11))
        .putRequired(INSTITUTION_ID, createIdentifiers(list.get(4), INSTITUTION_IDENTIFIERS))
        .putRequired(INSTITUTION_NAME, list.get(3))
        .putRequired(REGISTRY_RECORD, list.get(12))
        .putOptional(REPORT_ATTRIBUTES, createReportAttributes(list.get(7)))
        .putRequired(
            REPORT_FILTERS,
            createReportFilters(String.join(SEMICOLON_SPACE.getValue(), list.get(6), list.get(9))))
        .putOptional(EXCEPTIONS, createExceptions(list.get(8)))
        .build();
  }
}
