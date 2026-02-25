package org.olf.erm.usage.counter51;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olf.erm.usage.counter51.JsonProperties.ATTRIBUTES_TO_SHOW;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ATTRIBUTES;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ID;
import static org.olf.erm.usage.counter51.ReportType.TR;
import static org.olf.erm.usage.counter51.ReportType.TR_J1;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_NO_REPORT_ID;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_REPORT_ATTRIBUTES_TEMPLATE;
import static org.olf.erm.usage.counter51.TestUtil.TR_WITH_INVALID_REPORT_HEADER;
import static org.olf.erm.usage.counter51.TestUtil.TR_WITH_INVALID_REPORT_ITEMS;
import static org.olf.erm.usage.counter51.TestUtil.getReportValidator;
import static org.olf.erm.usage.counter51.TestUtil.getResourcePath;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsObjectNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;
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

    ValidationResult method1Result = reportValidator.validateReport(report);
    assertThat(method1Result.isValid()).isTrue();

    ValidationResult method2Result = reportValidator.validateReport(report, reportType);
    assertThat(method2Result.isValid()).isTrue();

    assertThat(reportClone).isEqualTo(report);
  }

  @Test
  void testMissingReportID() throws IOException {
    ObjectNode report = readFileAsObjectNode(getSampleReportPath(TR).toFile());
    report.withObject(REPORT_HEADER).remove(REPORT_ID);

    assertThat(reportValidator.validateReport(report))
        .satisfies(isInvalidWithMessage(ERR_NO_REPORT_ID));
    assertThat(reportValidator.validateReport(report, TR))
        .satisfies(isInvalidWithMessage(ERR_NO_REPORT_ID));
  }

  @Test
  void testInvalidReportHeaderReportAttributes() throws IOException {
    ObjectNode report =
        readFileAsObjectNode(getResourcePath(TR_WITH_INVALID_REPORT_HEADER).toFile());

    assertThat(reportValidator.validateReport(report))
        .satisfies(isInvalidWithMessage(ERR_REPORT_ATTRIBUTES_TEMPLATE.formatted("")));
    assertThat(reportValidator.validateReport(report, TR))
        .satisfies(isInvalidWithMessage(ERR_REPORT_ATTRIBUTES_TEMPLATE.formatted("")));
  }

  @Test
  void testInvalidReportItems() throws IOException {
    ObjectNode report =
        readFileAsObjectNode(getResourcePath(TR_WITH_INVALID_REPORT_ITEMS).toFile());

    assertThat(reportValidator.validateReport(report))
        .satisfies(isInvalidWithMessage("Unrecognized field \"foo\""));
    assertThat(reportValidator.validateReport(report, TR))
        .satisfies(isInvalidWithMessage("Unrecognized field \"foo\""));
  }

  @Test
  void testInvalidClassDefinition() throws IOException {
    ObjectNode report = readFileAsObjectNode(getSampleReportPath(TR).toFile());

    assertThat(reportValidator.validateReport(report, TR_J1))
        .satisfies(isInvalidWithMessage("Unexpected value 'TR'"));
  }

  @ParameterizedTest
  @EnumSource(
      value = ReportType.class,
      names = {"TR", "DR", "IR", "PR"})
  void testValidReportsWithReversedAttributesToShow(ReportType reportType) throws IOException {
    ObjectNode report = readFileAsObjectNode(getSampleReportPath(reportType).toFile());
    ArrayNode attributesToShow =
        (ArrayNode) report.path(REPORT_HEADER).path(REPORT_ATTRIBUTES).path(ATTRIBUTES_TO_SHOW);

    List<JsonNode> reversedElements =
        StreamSupport.stream(attributesToShow.spliterator(), false).toList().reversed();
    attributesToShow.removeAll().addAll(reversedElements);

    assertThat(reportValidator.validateReport(report))
        .satisfies(res -> assertThat(res.isValid()).isTrue());
    assertThat(reportValidator.validateReport(report, reportType))
        .satisfies(res -> assertThat(res.isValid()).isTrue());
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
