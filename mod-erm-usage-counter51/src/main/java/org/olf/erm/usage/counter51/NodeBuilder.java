package org.olf.erm.usage.counter51;

import static org.olf.erm.usage.counter51.Counter51Utils.getDefaultObjectMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

/**
 * A builder class for creating JSON object nodes with proper type handling. This utility class
 * simplifies the creation of JSON objects by handling different value types and providing methods
 * for required and optional properties.
 */
class NodeBuilder {

  static final String UNSUPPORTED_TYPE_TEMPLATE = "Unsupported value type for key: %s";
  private final ObjectNode node;

  public NodeBuilder() {
    this.node = getDefaultObjectMapper().createObjectNode();
  }

  public NodeBuilder putRequired(String key, Object value) {
    switch (value) {
      case String s -> node.put(key, s);
      case Integer i -> node.put(key, i);
      case JsonNode jn -> node.set(key, jn);
      case List<?> l -> node.set(key, getDefaultObjectMapper().valueToTree(l));
      case null -> node.putNull(key);
      default -> throw new IllegalArgumentException(UNSUPPORTED_TYPE_TEMPLATE.formatted(key));
    }
    return this;
  }

  @SuppressWarnings(
      "java:S6916") // Suppress "Use when instead of a single if inside a pattern match body" as it
  // has different fall-through behavior
  public NodeBuilder putOptional(String key, Object value) {
    switch (value) {
      case String s -> {
        if (!s.isEmpty()) node.put(key, s);
      }
      case Integer i -> node.put(key, i);
      case JsonNode jn -> {
        if (!jn.isEmpty()) node.set(key, jn);
      }
      case List<?> l -> {
        if (!l.isEmpty()) node.set(key, getDefaultObjectMapper().valueToTree(l));
      }
      case null -> {
        // ignore null values for optional properties
      }
      default -> throw new IllegalArgumentException(UNSUPPORTED_TYPE_TEMPLATE.formatted(key));
    }
    return this;
  }

  public ObjectNode build() {
    return node;
  }
}
