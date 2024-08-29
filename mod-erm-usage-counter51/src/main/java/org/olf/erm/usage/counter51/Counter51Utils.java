package org.olf.erm.usage.counter51;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Counter51Utils {

  private Counter51Utils() {}

  public static ObjectMapper createDefaultObjectMapper() {
    return ObjectMapperFactory.createDefault();
  }
}
