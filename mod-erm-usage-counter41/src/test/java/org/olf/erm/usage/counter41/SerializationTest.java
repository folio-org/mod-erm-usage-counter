package org.olf.erm.usage.counter41;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import io.vertx.core.json.JsonObject;
import jakarta.xml.bind.JAXB;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.niso.schemas.counter.Report;

public class SerializationTest {

  @Test
  public void testSampleReport() throws URISyntaxException {
    URI uri = Resources.getResource("reportJSTOR.xml").toURI();
    Report report = JAXB.unmarshal(uri, Report.class);

    String json = Counter4Utils.toJSON(report);
    assertThat(new JsonObject(json).getString("created"))
        .isEqualTo("2018-10-24T08:37:25.730+01:00");
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
