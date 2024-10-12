package org.olf.erm.usage.counter51;

import static com.google.common.io.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter51.ValidationBeanDeserializerModifier.VALIDATION_FAILED_MSG;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.counter51client.model.TR;

class ObjectMapperTest {

  private static final String PACKAGE_PREFIX = "org.openapitools.counter51client.model.";
  private static final String REPORT_BASE_PATH = "report/";
  private static final String TR_FILENAME = "TR_sample_r51";
  private static final String TRJ1_FILENAME = "TRJ1_sample_r51";
  private static final String EXTENSION_JSON = ".json";
  private static ObjectMapper mapper;

  @BeforeAll
  static void beforeAll() {
    mapper = ObjectMapperFactory.createDefault();
  }

  private static Stream<String> getSampleReports() throws IOException {
    URL reportFolder = getResource(REPORT_BASE_PATH);

    Path path = new File(reportFolder.getFile()).toPath();
    Stream<String> result;
    try (Stream<Path> paths = Files.walk(path)) {
      result =
          paths
              .filter(Files::isRegularFile)
              .filter(p -> p.toString().endsWith(EXTENSION_JSON))
              .map(p -> p.getFileName().toString())
              .toList()
              .stream();
    }
    return result;
  }

  @ParameterizedTest
  @MethodSource("getSampleReports")
  void testDeserializationWithSampleReports(String filename)
      throws IOException, ClassNotFoundException {
    URL input = getResource(REPORT_BASE_PATH + filename);
    ObjectNode expected = new ObjectMapper().readValue(input, ObjectNode.class);

    String reportId = filename.split("_")[0];
    Object o = mapper.readValue(input, Class.forName(PACKAGE_PREFIX + reportId));
    ObjectNode actual = mapper.convertValue(o, ObjectNode.class);

    assertThat(actual).isEqualTo(expected);
  }

  @Nested
  class ValidationTest {

    @Test
    void emptyReport() {
      String emptyReport = "{}";

      assertThatThrownBy(() -> mapper.readValue(emptyReport, TR.class))
          .hasMessageContaining(VALIDATION_FAILED_MSG);
    }

    @Test
    void invalidStringSize() throws IOException {
      TR titleReport =
          mapper.readValue(getResource(REPORT_BASE_PATH + TR_FILENAME + EXTENSION_JSON), TR.class);
      titleReport.getReportHeader().setCreatedBy("A");

      assertThatThrownBy(() -> mapper.convertValue(titleReport, TR.class))
          .hasMessageContaining(VALIDATION_FAILED_MSG);
    }

    @Test
    void deserializeToIncorrectReportType() {
      assertThatThrownBy(
              () ->
                  mapper.readValue(
                      getResource(REPORT_BASE_PATH + TRJ1_FILENAME + EXTENSION_JSON), TR.class))
          .hasMessageContaining(VALIDATION_FAILED_MSG);
    }
  }
}
