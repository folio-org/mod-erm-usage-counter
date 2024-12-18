package org.olf.erm.usage.counter51;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class TestUtil {

  private static final ObjectMapper objectMapper = Counter51Utils.createDefaultObjectMapper();
  private static final ReportValidator reportValidator = new ReportValidator(objectMapper);
  private static final ReportConverter reportConverter = new ReportConverter(objectMapper);
  static final String SAMPLE_REPORTS_PATH_TEMPLATE = "sample-reports/%s_sample_r51.json";
  static final String TR_WITH_EXCEPTION = "TR_r51_with_exception.json";
  static final String TR_WITH_INVALID_REPORT_HEADER = "TR_r51_with_invalid_report_header.json";

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

  static Path getSampleReportPath(String reportName) {
    return getResourcePath(SAMPLE_REPORTS_PATH_TEMPLATE.formatted(reportName));
  }

  static Path getSampleReportPath(ReportType reportType) {
    String reportName = reportType.toString().replace("_", "");
    return getResourcePath(SAMPLE_REPORTS_PATH_TEMPLATE.formatted(reportName));
  }

  static Path getResourcePath(String resourceName) {
    return Paths.get(Resources.getResource(resourceName).getPath());
  }
}
