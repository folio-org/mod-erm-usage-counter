package org.olf.erm.usage.counter51;

import static java.util.Collections.emptyList;
import static java.util.Spliterator.ORDERED;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.Delimiter.EQUALS;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.Delimiter.SEMICOLON_SPACE;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class ReportCsvFieldExtractor {

  private static final List<String> AUTHOR_IDS = List.of("ISNI", "ORCID");
  private static final String PROPRIETARY = "Proprietary";

  public enum Delimiter {
    EQUALS("="),
    PIPE("|"),
    SEMICOLON_SPACE("; ");

    private final String value;

    Delimiter(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public static List<String> extractMetricTypes(JsonNode node) {
    return toStream(node.fieldNames()).toList();
  }

  public static String extractExceptions(JsonNode node) {
    return toStream(node.elements())
        .map(ReportCsvFieldExtractor::exceptionToString)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(SEMICOLON_SPACE.getValue()));
  }

  public static String extractReportingPeriod(JsonNode node) {
    return toStream(node.fields())
        .filter(e -> List.of("Begin_Date", "End_Date").contains(e.getKey()))
        .map(ReportCsvFieldExtractor::entryToString)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(SEMICOLON_SPACE.getValue()));
  }

  public static String extractKeyValuePairs(JsonNode node, List<String> excludeFields) {
    return toStream(node.fields())
        .filter(e -> !excludeFields.contains(e.getKey()))
        .map(ReportCsvFieldExtractor::entryToString)
        .collect(Collectors.joining(SEMICOLON_SPACE.getValue()));
  }

  private static String exceptionToString(JsonNode e) {
    String code = e.path("Code").asText();
    String message = e.path("Message").asText();
    String data = e.path("Data").asText();

    if (code.isEmpty() || message.isEmpty()) {
      return "";
    } else {
      return data.isEmpty()
          ? "%s: %s".formatted(code, message)
          : "%s: %s (%s)".formatted(code, message, data);
    }
  }

  private static String entryToString(Entry<String, JsonNode> e) {
    String key = e.getKey();
    String value = extractValues(e.getValue(), Delimiter.PIPE);
    return key.isEmpty() || value.isEmpty() ? "" : String.join(EQUALS.getValue(), key, value);
  }

  public static String extractKeyValuePairs(JsonNode node) {
    return extractKeyValuePairs(node, Collections.emptyList());
  }

  public static String extractValues(JsonNode node, Delimiter delimiter) {
    return node.isArray()
        ? toStream(node.elements())
            .map(JsonNode::asText)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(delimiter.getValue()))
        : node.asText();
  }

  public static List<String> extractUsageData(JsonNode node, List<String> months) {
    List<String> monthlyValues = months.stream().map(ym -> node.path(ym).asText()).toList();
    int total = monthlyValues.stream().filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).sum();

    if (total == 0) {
      return emptyList();
    }

    return Stream.concat(Stream.of(total), monthlyValues.stream()).map(String::valueOf).toList();
  }

  public static List<String> extractValuesViaMapping(
      JsonNode node, ReportType reportType, ReportCsvMapping mapping) {
    return mapping.getMappingFunctions(reportType).stream().map(f -> f.apply(node)).toList();
  }

  public static String extractAuthors(JsonNode node) {
    return node.isArray()
        ? toStream(node.elements())
            .map(
                e -> {
                  String name = e.path("Name").asText();
                  if (name.isEmpty()) {
                    return null;
                  }
                  return getAuthorId(e).map(id -> "%s (%s)".formatted(name, id)).orElse(name);
                })
            .filter(Objects::nonNull)
            .collect(Collectors.joining(SEMICOLON_SPACE.getValue()))
        : "";
  }

  private static Optional<String> getAuthorId(JsonNode node) {
    return toStream(node.fields())
        .filter(e -> AUTHOR_IDS.contains(e.getKey()))
        .filter(e -> !e.getValue().asText().isEmpty())
        .findFirst()
        .map(e -> "%s:%s".formatted(e.getKey(), e.getValue().asText()));
  }

  public static String extractIdentifiers(JsonNode node) {
    return toStream(node.fields())
        .map(
            e -> {
              JsonNode value = e.getValue();
              if (value.isArray() && !value.isEmpty() && !value.path(0).asText().isEmpty()) {
                return PROPRIETARY.equals(e.getKey())
                    ? value.path(0).asText()
                    : "%s:%s".formatted(e.getKey(), value.path(0).asText());
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.joining(SEMICOLON_SPACE.getValue()));
  }

  public static String extractPlatform(JsonNode node) {
    return node.path("Platform").asText();
  }

  private static <T> Stream<T> toStream(Iterator<T> iterator) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, ORDERED), false);
  }
}
