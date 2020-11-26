package org.olf.erm.usage.counter41;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.JAXB;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.Test;
import org.niso.schemas.counter.Report;

public class SerializationTest {

  private static final ObjectMapper mapper = Counter4Utils.createObjectMapper();

  private void testXMLGregCalString(String datetime) {
    try {
      XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(datetime);
      String str = mapper.writeValueAsString(date);
      XMLGregorianCalendar cal = mapper.readValue(str, XMLGregorianCalendar.class);
      assertThat(date).hasToString(datetime);
      assertThat(str).isEqualTo(mapper.writeValueAsString(datetime));
      assertThat(cal).hasToString(datetime);
    } catch (IOException | DatatypeConfigurationException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testXMLGregCalStrings() {
    // +00:00 gets replaced by Z
    testXMLGregCalString("2018-10-24T08:37:25.730+01:00");
    testXMLGregCalString("2018-10-24");
    testXMLGregCalString("2018-10-24T08:37:25.730Z");
    testXMLGregCalString("2018-10-24T12:40:00.000Z");
    testXMLGregCalString("2018-10-24T12:40:00+02:00");
  }

  @Test
  public void testSampleReport() throws URISyntaxException {
    URI uri = Resources.getResource("reportJSTOR.xml").toURI();
    Report report = JAXB.unmarshal(uri, Report.class);

    String json = Counter4Utils.toJSON(report);
    Report fromJSON = Counter4Utils.fromJSON(json);

    assertThat(fromJSON).usingRecursiveComparison().isEqualTo(report);
  }

  @Test
  public void testSampleReportWithoutFractionalSeconds() throws URISyntaxException {
    URI uri = Resources.getResource("reportJSTORwofracSeconds.xml").toURI();
    Report report = JAXB.unmarshal(uri, Report.class);

    String json = Counter4Utils.toJSON(report);
    Report fromJSON = Counter4Utils.fromJSON(json);

    assertThat(fromJSON).usingRecursiveComparison().isEqualTo(report);
  }
}
