package org.olf.erm.usage.counter51;

import static java.util.Optional.ofNullable;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ATTRIBUTES;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ID;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_NO_REPORT_ID;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_RELEASE_TEMPLATE;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_REPORT_ATTRIBUTES_TEMPLATE;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_REPORT_ID_TEMPLATE;
import static org.olf.erm.usage.counter51.ReportValidator.ErrorMessages.ERR_UNSUPPORTED_REPORT_ID_TEMPLATE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportValidator {

  static final String RELEASE = "5.1";
  private static final String REPORT_CLASS_NAME_TEMPLATE =
      "org.openapitools.counter51client.model.%s";
  private final ObjectMapper objectMapper;

  public ReportValidator(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ValidationResult validateReport(JsonNode report, ReportType reportType) {
    return validateByDefintions(report, reportType);
  }

  public ValidationResult validateReport(JsonNode report) {
    return validateByDefintions(report, null);
  }

  private String getReportId(JsonNode report) {
    return report.path(REPORT_HEADER).path(REPORT_ID).asText();
  }

  private String getReportRelease(JsonNode report) {
    return report.path(REPORT_HEADER).path(JsonProperties.RELEASE).asText();
  }

  private ValidationResult validateByDefintions(JsonNode report, ReportType reportType) {
    String reportId = getReportId(report);

    if ("".equals(reportId)) {
      return ValidationResult.error(ERR_NO_REPORT_ID);
    }

    if (reportType == null) {
      try {
        reportType = ReportType.valueOf(reportId);
      } catch (IllegalArgumentException e) {
        return ValidationResult.error(ERR_UNSUPPORTED_REPORT_ID_TEMPLATE, reportId);
      }
    }

    ValidationResult classValidationResult = validateClassDefinition(report, reportType);
    if (!classValidationResult.isValid()) {
      return classValidationResult;
    }
    ValidationResult reportIdValidationResult = validateReportId(reportId, reportType);
    if (!reportIdValidationResult.isValid()) {
      return reportIdValidationResult;
    }
    ValidationResult reportReleaseValidationResult = validateReportRelease(report);
    if (!reportReleaseValidationResult.isValid()) {
      return reportReleaseValidationResult;
    }
    return validateReportHeaderReportAttributes(report, reportType);
  }

  private ValidationResult validateClassDefinition(JsonNode report, ReportType reportType) {
    try {
      objectMapper.convertValue(report, getReportClass(reportType));
    } catch (Exception e) {
      return ValidationResult.error(e);
    }
    return ValidationResult.success();
  }

  private ValidationResult validateReportId(String reportId, ReportType reportType) {
    String expectedReportID = reportType.toString();
    if (!expectedReportID.equals(reportId)) {
      return ValidationResult.error(ERR_REPORT_ID_TEMPLATE, expectedReportID);
    }
    return ValidationResult.success();
  }

  private ValidationResult validateReportRelease(JsonNode report) {
    if (!RELEASE.equals(getReportRelease(report))) {
      return ValidationResult.error(ERR_RELEASE_TEMPLATE, RELEASE);
    }
    return ValidationResult.success();
  }

  private ValidationResult validateReportHeaderReportAttributes(
      JsonNode report, ReportType reportType) {
    JsonNode reportAttributes =
        ofNullable(report.path(REPORT_HEADER).get(REPORT_ATTRIBUTES))
            .orElse(objectMapper.createObjectNode());
    ObjectNode expectedReportAttributes =
        objectMapper.convertValue(
            reportType.getProperties().getReportAttributes(), ObjectNode.class);
    if (reportAttributes.equals(expectedReportAttributes)) {
      return ValidationResult.success();
    } else {
      return ValidationResult.error(
          ERR_REPORT_ATTRIBUTES_TEMPLATE, expectedReportAttributes.toString());
    }
  }

  private Class<?> getReportClass(ReportType reportType) throws ClassNotFoundException {
    String reportName = reportType.toString().replace("_", "");
    String headerClassName = REPORT_CLASS_NAME_TEMPLATE.formatted(reportName);
    return Class.forName(headerClassName);
  }

  static class ValidationResult {
    private final boolean isValid;
    private final String errorMessage;

    private ValidationResult(boolean isValid, String errorMessage) {
      this.isValid = isValid;
      this.errorMessage = errorMessage;
    }

    public static ValidationResult success() {
      return new ValidationResult(true, null);
    }

    public static ValidationResult error(String message, Object... args) {
      return new ValidationResult(false, String.format(message, args));
    }

    public static ValidationResult error(Throwable error) {
      Throwable rootCause = ExceptionUtils.getRootCause(error);
      String message = (rootCause != null) ? rootCause.getMessage() : error.getMessage();
      return new ValidationResult(false, message);
    }

    public boolean isValid() {
      return isValid;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }

  public static class ReportValidatorException extends RuntimeException {

    public ReportValidatorException(String message) {
      super(message);
    }
  }

  static class ErrorMessages {

    static final String ERR_NO_REPORT_ID = "Could not find 'reportId'";
    static final String ERR_RELEASE_TEMPLATE = "Expected 'release' to be: '%s'";
    static final String ERR_REPORT_ATTRIBUTES_TEMPLATE = "Expected 'reportAttributes' to be: %s";
    static final String ERR_REPORT_ID_TEMPLATE = "Expected 'reportId' to be: %s";
    static final String ERR_UNSUPPORTED_REPORT_ID_TEMPLATE = "Unsupported 'reportId': %s";

    private ErrorMessages() {}
  }
}
