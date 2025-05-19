package org.olf.erm.usage.counter51;

import static java.util.Collections.emptyList;
import static org.olf.erm.usage.counter51.Delimiter.COLON;
import static org.olf.erm.usage.counter51.Delimiter.EQUALS;
import static org.olf.erm.usage.counter51.Delimiter.PIPE;
import static org.olf.erm.usage.counter51.Delimiter.SEMICOLON_SPACE;
import static org.olf.erm.usage.counter51.JsonProperties.ATTRIBUTES_TO_SHOW;
import static org.olf.erm.usage.counter51.JsonProperties.BEGIN_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.END_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.EXCEPTION_CODE;
import static org.olf.erm.usage.counter51.JsonProperties.EXCEPTION_DATA;
import static org.olf.erm.usage.counter51.JsonProperties.EXCEPTION_MESSAGE;
import static org.olf.erm.usage.counter51.JsonProperties.PLATFORM;
import static org.olf.erm.usage.counter51.JsonProperties.PROPRIETARY;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_ACCESS_METHOD;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_ACCESS_TYPE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_DATA_TYPE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_METRIC_TYPE;
import static org.olf.erm.usage.counter51.JsonProperties.YOP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openapitools.counter51client.model.AuthorsInner;
import org.openapitools.counter51client.model.ItemID;

/**
 * Utility class for processing COUNTER 5.1 report fields. This class provides methods for
 * extracting and creating various data elements from COUNTER 5.1 reports in both JSON and string
 * formats, including exception handling, processing author information, extracting identifiers, and
 * processing key-value pairs.
 */
class ReportFieldProcessor {

  public static final Pattern EXCEPTION_PATTERN =
      Pattern.compile("^([^:]+): ([^()]+)(?: \\(([^)]+)\\))?$");

  public static final Pattern AUTHOR_PATTERN = Pattern.compile("^([^()]+)(?: \\(([^)]+)\\))?$");

  private static final ObjectMapper objectMapper = Counter51Utils.getDefaultObjectMapper();
  static final List<String> ARRAY_KEYS_REPORT_ATTRIBUTES = List.of(ATTRIBUTES_TO_SHOW);
  static final List<String> ARRAY_KEYS_REPORT_FILTERS =
      List.of(
          REPORT_FILTERS_ACCESS_METHOD,
          REPORT_FILTERS_ACCESS_TYPE,
          REPORT_FILTERS_DATA_TYPE,
          REPORT_FILTERS_METRIC_TYPE,
          YOP);

  private ReportFieldProcessor() {}

  public static List<String> extractMetricTypes(JsonNode node) {
    return toStream(node.fieldNames()).toList();
  }

  public static String extractExceptions(JsonNode node) {
    return toStream(node.elements())
        .map(ReportFieldProcessor::exceptionToString)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(SEMICOLON_SPACE.getValue()));
  }

  public static JsonNode createExceptions(String s) {
    String[] split = s.split(SEMICOLON_SPACE.getValue());
    return Arrays.stream(split)
        .map(ReportFieldProcessor::stringToException)
        .filter(node -> !node.isEmpty())
        .collect(Collector.of(objectMapper::createArrayNode, ArrayNode::add, ArrayNode::addAll));
  }

  public static String extractReportingPeriod(JsonNode node) {
    return toStream(node.fields())
        .filter(e -> List.of(BEGIN_DATE, END_DATE).contains(e.getKey()))
        .sorted(Entry.comparingByKey())
        .map(ReportFieldProcessor::entryToString)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(SEMICOLON_SPACE.getValue()));
  }

  public static String extractKeyValuePairs(JsonNode node, List<String> excludeFields) {
    return toStream(node.fields())
        .filter(e -> !excludeFields.contains(e.getKey()))
        .map(ReportFieldProcessor::entryToString)
        .collect(Collectors.joining(SEMICOLON_SPACE.getValue()));
  }

  private static ObjectNode createKeyValuePair(String s, List<String> arrayKeys) {
    String[] split = s.split(EQUALS.getValue());
    String key = split[0];
    String value = split.length > 1 ? split[1] : "";

    List<String> values = Arrays.asList(value.split(Pattern.quote(PIPE.getValue())));

    Object finalValue = values.size() > 1 || arrayKeys.contains(key) ? values : values.get(0);
    return new NodeBuilder().putOptional(key, finalValue).build();
  }

  public static ObjectNode createKeyValuePairs(String s, List<String> arrayKeys) {
    String[] split = s.split(SEMICOLON_SPACE.getValue());
    return Arrays.stream(split)
        .map(str -> createKeyValuePair(str, arrayKeys))
        .reduce(ObjectNode::setAll)
        .orElse(objectMapper.createObjectNode());
  }

  public static ObjectNode createKeyValuePairs(String s) {
    return createKeyValuePairs(s, Collections.emptyList());
  }

  public static ObjectNode createReportAttributes(String s) {
    return createKeyValuePairs(s, ARRAY_KEYS_REPORT_ATTRIBUTES);
  }

  public static ObjectNode createReportFilters(String s) {
    return createKeyValuePairs(s, ARRAY_KEYS_REPORT_FILTERS);
  }

  public static ObjectNode createIdentifiers(String s, List<String> knownNamespaces) {
    String[] identifiers = s.split(SEMICOLON_SPACE.getValue());
    NodeBuilder nodeBuilder = new NodeBuilder();
    for (String identifier : identifiers) {
      String[] split = identifier.split(COLON.getValue());
      String namespace = split[0];
      String value = split.length > 1 ? split[1] : "";
      if (knownNamespaces.contains(namespace)) {
        nodeBuilder.putOptional(namespace, objectMapper.createArrayNode().add(value));
      } else {
        nodeBuilder.putOptional(PROPRIETARY, objectMapper.createArrayNode().add(identifier));
      }
    }
    return nodeBuilder.build();
  }

  public static ObjectNode createItemId(
      String doi,
      String proprietary,
      String isbn,
      String printIssn,
      String onlineIssn,
      String uri) {
    return new NodeBuilder()
        .putOptional(ItemID.JSON_PROPERTY_D_O_I, doi)
        .putOptional(ItemID.JSON_PROPERTY_PROPRIETARY, proprietary)
        .putOptional(ItemID.JSON_PROPERTY_I_S_B_N, isbn)
        .putOptional(ItemID.JSON_PROPERTY_ONLINE_I_S_S_N, onlineIssn)
        .putOptional(ItemID.JSON_PROPERTY_PRINT_I_S_S_N, printIssn)
        .putOptional(ItemID.JSON_PROPERTY_U_R_I, uri)
        .build();
  }

  public static ObjectNode createItemId(String proprietary) {
    return createItemId(null, proprietary, null, null, null, null);
  }

  private static String exceptionToString(JsonNode e) {
    String code = e.path(EXCEPTION_CODE).asText();
    String message = e.path(EXCEPTION_MESSAGE).asText();
    String data = e.path(EXCEPTION_DATA).asText();

    if (code.isEmpty() || message.isEmpty()) {
      return "";
    } else {
      return data.isEmpty()
          ? "%s: %s".formatted(code, message)
          : "%s: %s (%s)".formatted(code, message, data);
    }
  }

  private static ObjectNode stringToException(String s) {
    Matcher matcher = EXCEPTION_PATTERN.matcher(s);

    NodeBuilder nodeBuilder = new NodeBuilder();
    if (matcher.matches()) {
      String code = matcher.group(1);
      String message = matcher.group(2);
      String data = matcher.group(3);

      nodeBuilder.putRequired(EXCEPTION_CODE, code);
      nodeBuilder.putRequired(EXCEPTION_MESSAGE, message);
      nodeBuilder.putOptional(EXCEPTION_DATA, data);
    }
    return nodeBuilder.build();
  }

  private static String entryToString(Entry<String, JsonNode> e) {
    String key = e.getKey();
    String value = extractValues(e.getValue(), PIPE);
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

  public static String extractAuthors(JsonNode node) {
    return node.isArray()
        ? toStream(node.elements())
            .map(
                e -> {
                  String name = e.path(AuthorsInner.JSON_PROPERTY_NAME).asText();
                  if (name.isEmpty()) {
                    return null;
                  }
                  return getAuthorId(e).map(id -> "%s (%s)".formatted(name, id)).orElse(name);
                })
            .filter(Objects::nonNull)
            .collect(Collectors.joining(SEMICOLON_SPACE.getValue()))
        : "";
  }

  public static ArrayNode createAuthors(String s) {
    if (s.isEmpty()) {
      return objectMapper.createArrayNode();
    }

    String[] split = s.split(SEMICOLON_SPACE.getValue());
    return Arrays.stream(split)
        .map(
            str -> {
              Matcher matcher = AUTHOR_PATTERN.matcher(str);
              NodeBuilder authorNodeBuilder = new NodeBuilder();
              if (matcher.matches()) {
                String name = matcher.group(1);
                authorNodeBuilder.putRequired(AuthorsInner.JSON_PROPERTY_NAME, name);

                String identifier = matcher.group(2);
                if (identifier != null && !identifier.isEmpty()) {
                  String[] split1 = identifier.split(COLON.getValue());
                  String key = split1[0];
                  String value = split1.length > 1 ? split1[1] : "";
                  authorNodeBuilder.putOptional(key, value);
                }
              }
              return authorNodeBuilder.build();
            })
        .collect(Collector.of(objectMapper::createArrayNode, ArrayNode::add, ArrayNode::addAll));
  }

  private static Optional<String> getAuthorId(JsonNode node) {
    return toStream(node.fields())
        .filter(e -> IdentifierNamespaces.AUTHOR_IDENTIFIERS.contains(e.getKey()))
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
    return node.path(PLATFORM).asText();
  }

  private static <T> Stream<T> toStream(Iterator<T> iterator) {
    return Streams.stream(iterator);
  }
}
