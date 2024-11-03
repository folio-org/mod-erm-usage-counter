package org.olf.erm.usage.counter51;

import static com.google.common.io.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter51.ValidationBeanDeserializerModifier.VALIDATION_FAILED_MSG;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.counter51client.model.TR;

class ObjectMapperTest {

  private static final String PACKAGE_PREFIX = "org.openapitools.counter51client.model.";
  private static final String REPORT_BASE_PATH = "sample-reports/";
  private static final String TR_FILENAME = "TR_sample_r51";
  private static final String TRJ1_FILENAME = "TRJ1_sample_r51";
  private static final String EXTENSION_JSON = ".json";
  private static ObjectMapper mapper;

  @BeforeAll
  static void beforeAll() {
    mapper = ObjectMapperFactory.createDefault();
  }

  private static Path getPathForResource(String resourceName) {
    URL url = getResource(resourceName);
    try {
      return Paths.get(url.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private static Stream<Arguments> getSampleReports() throws IOException, URISyntaxException {
    Path sampleReportsDir = Paths.get(getResource(REPORT_BASE_PATH).toURI());
    Stream<Path> sampleReports;
    try (Stream<Path> paths = Files.walk(sampleReportsDir)) {
      sampleReports =
          paths
              .filter(Files::isRegularFile)
              .filter(p -> p.toString().endsWith(EXTENSION_JSON))
              .toList()
              .stream();
    }
    Stream<Path> additionalReports =
        Stream.of("TR_r51_with_exception.json").map(ObjectMapperTest::getPathForResource);
    return Stream.concat(sampleReports, additionalReports)
        .map(p -> Arguments.of(p, p.getFileName().toString()));
  }

  @ParameterizedTest(name = "[{index}] {1}")
  @MethodSource("getSampleReports")
  void testDeserializationWithSampleReports(Path path, String filename)
      throws IOException, ClassNotFoundException {
    URL input = path.toUri().toURL();
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
