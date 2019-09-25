package org.olf.erm.usage.counter41;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLGregorianCalendarDeserializer extends StdDeserializer<XMLGregorianCalendar> {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(XMLGregorianCalendarDeserializer.class);

  XMLGregorianCalendarDeserializer() {
    super(XMLGregorianCalendar.class);
  }

  @Override
  public XMLGregorianCalendar deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {

    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(p.getValueAsString());
    } catch (DatatypeConfigurationException e) {
      LOG.error(e.getMessage(), e);
    }

    return null;
  }
}
