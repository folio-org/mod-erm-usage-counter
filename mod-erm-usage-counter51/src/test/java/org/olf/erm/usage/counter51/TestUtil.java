package org.olf.erm.usage.counter51;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

class TestUtil {

  private static final ObjectMapper objectMapper = Counter51Utils.getDefaultObjectMapper();
  private static final ReportValidator reportValidator = new ReportValidator(objectMapper);
  private static final ReportConverter reportConverter =
      new ReportConverter(objectMapper, reportValidator);
  static final String SAMPLE_REPORTS_PATH_TEMPLATE = "sample-reports/%s_sample_r51.%s";
  static final String TR_WITH_EXCEPTION = "TR_r51_with_exception.json";
  static final String TR_WITH_INVALID_EXCEPTION = "TR_r51_with_invalid_exception.json";
  static final String TR_WITH_INVALID_REPORT_HEADER = "TR_r51_with_invalid_report_header.json";
  static final String TR_WITH_INVALID_REPORT_ITEMS = "TR_r51_with_invalid_report_items.json";
  static final String DEFAULT_EXTENSION = "json";

  static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  static ReportValidator getReportValidator() {
    return reportValidator;
  }

  static ReportConverter getReportConverter() {
    return reportConverter;
  }

  static ObjectNode readFileAsObjectNode(File file) throws IOException {
    return objectMapper.readValue(file, ObjectNode.class);
  }

  static List<String> readFileAsLines(Path path) throws IOException {
    return Files.readAllLines(path);
  }

  static Path getSampleReportPath(String reportName) {
    return getResourcePath(SAMPLE_REPORTS_PATH_TEMPLATE.formatted(reportName, DEFAULT_EXTENSION));
  }

  static Path getSampleReportPath(String reportName, String extension) {
    return getResourcePath(SAMPLE_REPORTS_PATH_TEMPLATE.formatted(reportName, extension));
  }

  static Path getSampleReportPath(ReportType reportType, String extension) {
    String reportName = reportType.toString().replace("_", "");
    return getResourcePath(SAMPLE_REPORTS_PATH_TEMPLATE.formatted(reportName, extension));
  }

  static Path getSampleReportPath(ReportType reportType) {
    String reportName = reportType.toString().replace("_", "");
    return getResourcePath(SAMPLE_REPORTS_PATH_TEMPLATE.formatted(reportName, DEFAULT_EXTENSION));
  }

  static Path getResourcePath(String resourceName) {
    return Paths.get(Resources.getResource(resourceName).getPath());
  }

  static List<String> getLinesFromString(String str, String separator) {
    return Arrays.asList(str.split(separator));
  }

  static List<String> removeBOMAndTrailingDelimiters(List<String> input, String delimiter) {
    return IntStream.range(0, input.size())
        .mapToObj(
            index -> {
              String line = input.get(index);
              if (index == 0) {
                line = line.replaceAll("^[\\uFEFF\\u200B\\u00A0]+", "");
              }
              return line.replaceAll(delimiter + "+$", "");
            })
        .toList();
  }

  static void assertThatReportLinesAreEqualIgnoringOrder(
      List<String> actualLines, List<String> expectedLines) {
    assertThat(actualLines).hasSameSizeAs(expectedLines);
    int size = actualLines.size();

    // check that header and column headings are equal
    assertThat(actualLines.subList(0, 15)).isEqualTo(expectedLines.subList(0, 15));
    // check that body rows are equal, ignoring order of rows
    assertThat(actualLines.subList(15, size))
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedLines.subList(15, size));
  }
}
