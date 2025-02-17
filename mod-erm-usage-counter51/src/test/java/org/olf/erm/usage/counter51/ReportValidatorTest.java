package org.olf.erm.usage.counter51;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olf.erm.usage.counter51.JsonProperties.RELEASE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ATTRIBUTES;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ID;
import static org.olf.erm.usage.counter51.ReportType.TR;
import static org.olf.erm.usage.counter51.ReportType.TR_J1;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_NO_REPORT_ID;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_RELEASE_TEMPLATE;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_REPORT_ATTRIBUTES_TEMPLATE;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_REPORT_ID_TEMPLATE;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_UNSUPPORTED_REPORT_ID_TEMPLATE;
import static org.olf.erm.usage.counter51.TestUtil.TR_WITH_INVALID_REPORT_HEADER;
import static org.olf.erm.usage.counter51.TestUtil.getReportValidator;
import static org.olf.erm.usage.counter51.TestUtil.getResourcePath;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsObjectNode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import org.assertj.core.api.ThrowingConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.olf.erm.usage.counter51.ReportValidator.ValidationResult;

class ReportValidatorTest {

  private final ReportValidator reportValidator = getReportValidator();

  @ParameterizedTest
  @EnumSource(ReportType.class)
  void testValidReports(ReportType reportType) throws IOException {
    File reportFile = getSampleReportPath(reportType).toFile();
    ObjectNode report = readFileAsObjectNode(reportFile);
    ObjectNode reportClone = report.deepCopy();

    ValidationResult method1Result = reportValidator.validateReportHeader(report);
    assertThat(method1Result.isValid()).isTrue();

    ValidationResult method2Result = reportValidator.validateReportHeader(report, reportType);
    assertThat(method2Result.isValid()).isTrue();

    assertThat(reportClone).isEqualTo(report);
  }

  @Test
  void testInvalidReleaseVersion() throws IOException {
    ObjectNode report = readFileAsObjectNode(getSampleReportPath(TR).toFile());
    report.withObject(REPORT_HEADER).put(RELEASE, "5.0");

    assertThat(reportValidator.validateReportHeader(report))
        .satisfies(
            res -> {
              assertThat(res.isValid()).isFalse();
              assertThat(res.getErrorMessage())
                  .contains(ERR_RELEASE_TEMPLATE.formatted(ReportValidator.RELEASE));
            });
  }

  @Test
  void testInvalidReportID() throws IOException {
    ObjectNode report = readFileAsObjectNode(getSampleReportPath(TR).toFile());
    String invalidReportID = "TR_2";
    report.withObject(REPORT_HEADER).put(REPORT_ID, invalidReportID);

    assertThat(reportValidator.validateReportHeader(report))
        .satisfies(
            isInvalidWithMessage(ERR_UNSUPPORTED_REPORT_ID_TEMPLATE.formatted(invalidReportID)));
    assertThat(reportValidator.validateReportHeader(report, TR))
        .satisfies(isInvalidWithMessage(ERR_REPORT_ID_TEMPLATE.formatted(TR)));
  }

  @Test
  void testMissingReportID() throws IOException {
    ObjectNode report = readFileAsObjectNode(getSampleReportPath(TR).toFile());
    report.withObject(REPORT_HEADER).remove(REPORT_ID);

    assertThat(reportValidator.validateReportHeader(report))
        .satisfies(isInvalidWithMessage(ERR_NO_REPORT_ID));
    assertThat(reportValidator.validateReportHeader(report, TR))
        .satisfies(isInvalidWithMessage(ERR_NO_REPORT_ID));
  }

  @Test
  void testInvalidReportHeaderReportAttributes() throws IOException {
    ObjectNode report =
        readFileAsObjectNode(getResourcePath(TR_WITH_INVALID_REPORT_HEADER).toFile());

    assertThat(reportValidator.validateReportHeader(report))
        .satisfies(isInvalidWithMessage(ERR_REPORT_ATTRIBUTES_TEMPLATE.formatted("")));
    assertThat(reportValidator.validateReportHeader(report, TR))
        .satisfies(isInvalidWithMessage(ERR_REPORT_ATTRIBUTES_TEMPLATE.formatted("")));
  }

  @Test
  void testInvalidClassDefinition() throws IOException {
    ObjectNode report = readFileAsObjectNode(getSampleReportPath(TR).toFile());

    assertThat(reportValidator.validateReportHeader(report, TR_J1))
        .satisfies(isInvalidWithMessage(REPORT_ATTRIBUTES));
  }

  private ThrowingConsumer<ValidationResult> isInvalidWithMessage(String message) {
    return validationResult ->
        assertThat(validationResult)
            .satisfies(
                res -> {
                  assertThat(res.isValid()).isFalse();
                  assertThat(res.getErrorMessage()).contains(message);
                });
  }
}
