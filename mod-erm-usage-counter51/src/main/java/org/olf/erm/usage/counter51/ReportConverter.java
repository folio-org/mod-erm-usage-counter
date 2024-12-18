package org.olf.erm.usage.counter51;

import static org.olf.erm.usage.counter51.JsonProperties.ATTRIBUTE_PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.ITEMS;
import static org.olf.erm.usage.counter51.JsonProperties.PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ATTRIBUTES;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_ACCESS_METHOD;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_ACCESS_TYPE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_DATA_TYPE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_METRIC_TYPE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ID;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ITEMS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_NAME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.olf.erm.usage.counter51.ReportValidator.ValidationResult;

class ReportConverter {

  static final String ERR_STANDARD_VIEW_TEMPLATE =
      "Report conversion is only supported for Standard Views: %s";
  static final String ERR_INVALID_REPORT_TEMPLATE = "Supplied report is not valid: %s";
  private final ObjectMapper objectMapper;
  private final ReportValidator validator;

  public ReportConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.validator = new ReportValidator(objectMapper);
  }

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
  public ObjectNode convert(ObjectNode report, ReportType reportType) {
    if (!ReportType.getStandardViews().contains(reportType)) {
      throw new ReportConverterException(
          ERR_STANDARD_VIEW_TEMPLATE.formatted(ReportType.getStandardViews()));
    }

    ValidationResult validationResult =
        validator.validateReportHeader(report, reportType.getParentReportType());

    if (validationResult.isValid()) {
      JsonNode reportHeader = createReportHeader(report.withObject(REPORT_HEADER), reportType);
      JsonNode reportItems = filterReportItems(report.path(REPORT_ITEMS), reportType);

      ObjectNode result = objectMapper.createObjectNode();
      result.set(REPORT_HEADER, reportHeader);
      result.set(REPORT_ITEMS, reportItems);

      return result;
    } else {
      throw new ReportConverterException(
          ERR_INVALID_REPORT_TEMPLATE.formatted(validationResult.getErrorMessage()));
    }
  }

  private JsonNode createReportHeader(ObjectNode originalReportHeader, ReportType reportType) {
    ObjectNode reportHeader = originalReportHeader.deepCopy();
    ObjectNode reportFilters = reportHeader.withObjectProperty(REPORT_FILTERS);

    clearReportHeader(reportHeader);
    reportHeader.put(REPORT_ID, reportType.toString());
    reportHeader.put(REPORT_NAME, reportType.getReportName());

    clearReportFilters(reportFilters, reportType);
    putIfNotEmpty(
        reportFilters, REPORT_FILTERS_ACCESS_METHOD, reportType.getProperties().getAccessMethods());
    putIfNotEmpty(
        reportFilters, REPORT_FILTERS_ACCESS_TYPE, reportType.getProperties().getAccessTypes());
    putIfNotEmpty(
        reportFilters, REPORT_FILTERS_DATA_TYPE, reportType.getProperties().getDataTypes());
    putIfNotEmpty(
        reportFilters, REPORT_FILTERS_METRIC_TYPE, reportType.getProperties().getMetricTypes());

    return reportHeader;
  }

  private void putIfNotEmpty(ObjectNode objectNode, String key, List<String> strings) {
    if (!strings.isEmpty()) {
      objectNode.set(key, objectMapper.valueToTree(strings));
    }
  }

  private void clearReportHeader(ObjectNode reportHeader) {
    reportHeader.remove(REPORT_ATTRIBUTES);
  }

  private void clearReportFilters(ObjectNode reportFilters, ReportType reportType) {
    reportFilters
        .fieldNames()
        .forEachRemaining(
            fieldName -> {
              if (!reportType.getProperties().getRequiredReportFilters().contains(fieldName)) {
                reportFilters.remove(fieldName);
              }
            });
  }

  private JsonNode filterReportItems(JsonNode reportItems, ReportType reportType) {
    ArrayNode filteredReportItems = objectMapper.createArrayNode();
    reportItems.forEach(
        reportItem -> {
          JsonNode filteredReportItem =
              reportType.isItemReport()
                  ? filterIRReportItem(reportItem, reportType)
                  : filterReportItem(reportItem, reportType);
          if (!filteredReportItem.isEmpty()) {
            filteredReportItems.add(filteredReportItem);
          }
        });
    return filteredReportItems; // TODO: merge items
  }

  private JsonNode filterIRReportItem(JsonNode reportItem, ReportType reportType) {
    ObjectNode filteredReportItem = objectMapper.createObjectNode();

    ArrayNode arrayNode = objectMapper.createArrayNode();
    reportItem
        .path(ITEMS)
        .forEach(
            item -> {
              JsonNode jsonNode = filterReportItem(item, reportType);
              if (!jsonNode.isEmpty()) {
                ((ObjectNode) jsonNode)
                    .remove(reportType.getProperties().getItemAttributesToRemove());
                arrayNode.add(jsonNode);
              }
            });
    if (!arrayNode.isEmpty()) {
      reportItem
          .fieldNames()
          .forEachRemaining(
              fieldName -> {
                if (ITEMS.equals(fieldName)) {
                  filteredReportItem.set(fieldName, arrayNode);
                } else if (!reportType
                    .getProperties()
                    .getParentItemAttributesToRemove()
                    .contains(fieldName)) {
                  filteredReportItem.set(fieldName, reportItem.get(fieldName));
                }
              });
    }

    return filteredReportItem;
  }

  private JsonNode filterReportItem(JsonNode reportItem, ReportType reportType) {
    ObjectNode filteredReportItem = objectMapper.createObjectNode();
    JsonNode filteredAttributePerformance =
        filterAttributePerformance(reportItem.path(ATTRIBUTE_PERFORMANCE), reportType);
    if (!filteredAttributePerformance.isEmpty()) {
      reportItem
          .fieldNames()
          .forEachRemaining(
              fieldName -> {
                if (ATTRIBUTE_PERFORMANCE.equals(fieldName)) {
                  filteredReportItem.set(fieldName, filteredAttributePerformance);
                } else {
                  filteredReportItem.set(fieldName, reportItem.get(fieldName));
                }
              });
    }
    return filteredReportItem;
  }

  private JsonNode filterAttributePerformance(
      JsonNode attributePerformance, ReportType reportType) {
    ArrayNode filteredAttributePerformance = objectMapper.createArrayNode();
    attributePerformance.forEach(
        entry -> {
          ObjectNode filteredAttributePerformanceElement = objectMapper.createObjectNode();
          if (hasAttributes(entry, reportType)) {
            JsonNode filteredPerformance = filterPerformance(entry, reportType);
            if (!filteredPerformance.isEmpty()) {
              reportType
                  .getProperties()
                  .getPerformanceAttributes()
                  .forEach(
                      attribute ->
                          filteredAttributePerformanceElement.set(attribute, entry.get(attribute)));
              filteredAttributePerformanceElement.set(PERFORMANCE, filteredPerformance);

              filteredAttributePerformance.add(filteredAttributePerformanceElement);
            }
          }
        });

    return mergePerformanceWithAttributes(filteredAttributePerformance);
  }

  private ArrayNode mergePerformanceWithAttributes(JsonNode attributePerformance) {
    Map<Map<String, String>, ObjectNode> mergedAttributeMap = new LinkedHashMap<>();

    for (JsonNode entry : attributePerformance) {
      // Extract non-Performance attributes into a map
      Map<String, String> attributes = new LinkedHashMap<>();
      entry
          .fields()
          .forEachRemaining(
              field -> {
                if (!PERFORMANCE.equals(field.getKey())) {
                  attributes.put(field.getKey(), field.getValue().asText());
                }
              });

      // Retrieve or initialize the merged performance for the given attributes
      mergedAttributeMap.putIfAbsent(attributes, objectMapper.createObjectNode());
      ObjectNode mergedPerformance = mergedAttributeMap.get(attributes);

      // Merge Performance metrics
      JsonNode performance = entry.get(PERFORMANCE);
      performance
          .fields()
          .forEachRemaining(
              metricEntry -> {
                String metric = metricEntry.getKey();
                JsonNode months = metricEntry.getValue();

                ObjectNode metricNode = mergedPerformance.withObject(metric);
                months
                    .fields()
                    .forEachRemaining(
                        monthEntry -> {
                          String month = monthEntry.getKey();
                          int value = monthEntry.getValue().asInt();

                          metricNode.put(
                              month,
                              metricNode.has(month)
                                  ? metricNode.get(month).asInt() + value
                                  : value);
                        });
              });
    }

    // Convert merged map to the required JSON structure
    ArrayNode resultArray = objectMapper.createArrayNode();
    mergedAttributeMap.forEach(
        (attributes, mergedPerformance) -> {
          ObjectNode entry = objectMapper.createObjectNode();
          attributes.forEach(entry::put);
          entry.set(PERFORMANCE, mergedPerformance);
          resultArray.add(entry);
        });

    return resultArray;
  }

  private boolean hasAttributes(JsonNode jsonNode, ReportType reportType) {
    return (reportType.getProperties().getDataTypes().isEmpty()
            || reportType
                .getProperties()
                .getDataTypes()
                .contains(jsonNode.path(REPORT_FILTERS_DATA_TYPE).asText()))
        && (reportType.getProperties().getAccessTypes().isEmpty()
            || reportType
                .getProperties()
                .getAccessTypes()
                .contains(jsonNode.path(REPORT_FILTERS_ACCESS_TYPE).asText()))
        && (reportType.getProperties().getAccessMethods().isEmpty()
            || reportType
                .getProperties()
                .getAccessMethods()
                .contains(jsonNode.path(REPORT_FILTERS_ACCESS_METHOD).asText()));
  }

  private JsonNode filterPerformance(JsonNode jsonNode, ReportType reportType) {
    ObjectNode result = objectMapper.createObjectNode();
    JsonNode performance = jsonNode.path(PERFORMANCE);

    reportType
        .getProperties()
        .getMetricTypes()
        .forEach(
            metricType -> {
              JsonNode value = performance.get(metricType);
              if (value != null) {
                result.set(metricType, value);
              }
            });
    return result;
  }

  static class ReportConverterException extends RuntimeException {

    public ReportConverterException(String message) {
      super(message);
    }
  }
}
