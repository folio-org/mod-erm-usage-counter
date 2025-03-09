package org.olf.erm.usage.counter51;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter51.TestUtil.TR_WITH_EXCEPTION;
import static org.olf.erm.usage.counter51.TestUtil.TR_WITH_INVALID_EXCEPTION;
import static org.olf.erm.usage.counter51.TestUtil.getObjectMapper;
import static org.olf.erm.usage.counter51.TestUtil.getResourcePath;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsObjectNode;
import static org.olf.erm.usage.counter51.ValidationBeanDeserializerModifier.VALIDATION_FAILED_MSG;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openapitools.counter51client.model.TR;

class ObjectMapperTest {
  private static final ObjectMapper objectMapper = getObjectMapper();
  private static final String PACKAGE_PREFIX = "org.openapitools.counter51client.model.";

  @ParameterizedTest
  @EnumSource(ReportType.class)
  void testDeserializationForEachReportType(ReportType reportType)
      throws IOException, ClassNotFoundException {
    testDeserialization(getSampleReportPath(reportType).toFile(), reportType);
  }

  @Test
  void testDeserializationForReportWithException() throws IOException, ClassNotFoundException {
    testDeserialization(getResourcePath(TR_WITH_EXCEPTION).toFile(), ReportType.TR);
  }

  @Test
  void testDeserializationForReportWithInvalidException() {
    assertThatThrownBy(
            () ->
                testDeserialization(
                    getResourcePath(TR_WITH_INVALID_EXCEPTION).toFile(), ReportType.TR))
        .isInstanceOf(JsonMappingException.class);
  }

  private void testDeserialization(File file, ReportType reportType)
      throws IOException, ClassNotFoundException {
    ObjectNode expected = readFileAsObjectNode(file);

    String className = PACKAGE_PREFIX + reportType.toString().replace("_", "");
    Object o = objectMapper.readValue(file, Class.forName(className));
    ObjectNode actual = objectMapper.convertValue(o, ObjectNode.class);

    assertThat(actual).isEqualTo(expected);
  }

  @Nested
  class ValidationTest {

    @Test
    void emptyReport() {
      String emptyReport = "{}";

      assertThatThrownBy(() -> objectMapper.readValue(emptyReport, TR.class))
          .hasMessageContaining(VALIDATION_FAILED_MSG);
    }

    @Test
    void invalidStringSize() throws IOException {

      TR titleReport =
          objectMapper.readValue(getSampleReportPath(ReportType.TR).toFile(), TR.class);
      titleReport.getReportHeader().setCreatedBy("A");

      assertThatThrownBy(() -> objectMapper.convertValue(titleReport, TR.class))
          .hasMessageContaining(VALIDATION_FAILED_MSG);
    }

    @Test
    void deserializeToIncorrectReportType() {
      assertThatThrownBy(
              () ->
                  objectMapper.readValue(getSampleReportPath(ReportType.TR_J1).toFile(), TR.class))
          .isInstanceOf(ValueInstantiationException.class);
    }
  }
}
