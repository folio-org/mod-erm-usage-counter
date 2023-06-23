package org.olf.erm.usage.counter41.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.ZonedDateTime;

public class ZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {

  public ZonedDateTimeDeserializer() {
    super(ZonedDateTime.class);
  }

  @Override
  public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return ZonedDateTime.parse(p.getValueAsString());
  }
}
