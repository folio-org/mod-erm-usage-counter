package org.olf.erm.usage.counter51;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter51.JsonProperties.COUNTRY_CODE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.ReportType.TR;
import static org.olf.erm.usage.counter51.ReportType.TR_J1;
import static org.olf.erm.usage.counter51.TestUtil.TR_WITH_INVALID_REPORT_HEADER;
import static org.olf.erm.usage.counter51.TestUtil.getReportConverter;
import static org.olf.erm.usage.counter51.TestUtil.getResourcePath;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsObjectNode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.olf.erm.usage.counter51.ReportConverter.ReportConverterException;

class ReportConverterTest {

  private final ReportConverter reportConverter = getReportConverter();

  @ParameterizedTest
  @EnumSource(value = ReportType.class, names = ".*_.*", mode = Mode.MATCH_ANY)
  void testThatReportConversionPreservesOriginalAndMatchesExpected(ReportType reportType)
      throws IOException {
    Path inputReportFilePath = getSampleReportPath(reportType.toString().substring(0, 2));
    Path expectedReportFilePath = getSampleReportPath(reportType.toString().replace("_", ""));

    ObjectNode report = readFileAsObjectNode(inputReportFilePath.toFile());
    ObjectNode originalReport = report.deepCopy();
    ObjectNode expectedReport = readFileAsObjectNode(expectedReportFilePath.toFile());

    ObjectNode convertedReport = reportConverter.convert(report, reportType);

    System.out.println(convertedReport.toPrettyString());
    assertThat(convertedReport)
        .as("Converted report should match the expected report for %s", reportType)
        .isEqualTo(expectedReport);
    assertThat(report)
        .as("Original report should remain unchanged after conversion for %s", reportType)
        .isEqualTo(originalReport);
  }

  @Test
  void testThatValidationThrowsException() throws IOException {
    Path inputReportFilePath = getResourcePath(TR_WITH_INVALID_REPORT_HEADER);
    ObjectNode report = readFileAsObjectNode(inputReportFilePath.toFile());

    assertThatThrownBy(() -> reportConverter.convert(report, TR_J1))
        .isInstanceOf(ReportConverterException.class);
  }

  @Test
  void testThatAdditionalReportFiltersAreRemoved() throws IOException {
    Path inputReportFilePath = getSampleReportPath(TR);
    ObjectNode report = readFileAsObjectNode(inputReportFilePath.toFile());
    String countryCode = "DE";
    report.path(REPORT_HEADER).withObject(REPORT_FILTERS).put(COUNTRY_CODE, countryCode);

    ObjectNode convertedReport = reportConverter.convert(report, TR_J1);
    assertThat(report.path(REPORT_HEADER).path(REPORT_FILTERS).get(COUNTRY_CODE).asText())
        .isEqualTo(countryCode);
    assertThat(convertedReport.path(REPORT_HEADER).path(REPORT_FILTERS).get(COUNTRY_CODE)).isNull();
  }
}
