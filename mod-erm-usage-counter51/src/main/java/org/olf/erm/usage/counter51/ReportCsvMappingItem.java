package org.olf.erm.usage.counter51;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

class ReportCsvMappingItem {
  private final String name;
  private final List<ReportType> reports;
  private final Function<JsonNode, String> mappingFunction;

  private ReportCsvMappingItem(
      String name, List<ReportType> reports, Function<JsonNode, String> mappingFunction) {
    this.name = name;
    this.reports = reports;
    this.mappingFunction = mappingFunction;
  }

  public static ReportCsvMappingItem create(String name) {
    return create(name, name);
  }

  public static ReportCsvMappingItem create(String name, String strOrJsonPtrExpr) {
    return create(name, createMappingFunction(strOrJsonPtrExpr));
  }

  public static ReportCsvMappingItem create(String name, List<ReportType> reports) {
    return new ReportCsvMappingItem(name, reports, createMappingFunction(name));
  }

  public static ReportCsvMappingItem create(
      String name, List<ReportType> reports, String strOrJsonPtrExpr) {
    return new ReportCsvMappingItem(name, reports, createMappingFunction(strOrJsonPtrExpr));
  }

  public static ReportCsvMappingItem create(
      String name, List<ReportType> reports, Function<JsonNode, String> mappingFunction) {
    return new ReportCsvMappingItem(name, reports, mappingFunction);
  }

  public static ReportCsvMappingItem create(
      String name, Function<JsonNode, String> mappingFunction) {
    return new ReportCsvMappingItem(name, Arrays.asList(ReportType.values()), mappingFunction);
  }

  public String name() {
    return name;
  }

  public List<ReportType> reports() {
    return reports;
  }

  public Function<JsonNode, String> mappingFunction() {
    return mappingFunction;
  }

  private static Function<JsonNode, String> createMappingFunction(String strOrJsonPtrExpr) {
    String jsonPtrExpr =
        strOrJsonPtrExpr.startsWith("/") ? strOrJsonPtrExpr : "/" + strOrJsonPtrExpr;
    return jsonNode -> jsonNode.at(jsonPtrExpr).asText();
  }
}
