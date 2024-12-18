package org.olf.erm.usage.counter51;

import static java.util.Optional.ofNullable;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ATTRIBUTES;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportValidator {

  static final String ERR_NO_REPORT_ID = "Could not find 'reportId'";
  static final String ERR_RELEASE_TEMPLATE = "Expected 'release' to be: '%s'";
  static final String ERR_REPORT_ATTRIBUTES_TEMPLATE = "Expected 'reportAttributes' to be: %s";
  static final String ERR_REPORT_ID_TEMPLATE = "Expected 'reportId' to be: %s";
  static final String ERR_UNSUPPORTED_REPORT_ID_TEMPLATE = "Unsupported 'reportId': %s";
  static final String RELEASE = "5.1";
  private static final String HEADER_CLASS_NAME_TEMPLATE =
      "org.openapitools.counter51client.model.%sReportHeader";
  private final ObjectMapper objectMapper;

  public ReportValidator(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ValidationResult validateReportHeader(ObjectNode report, ReportType reportType) {
    String reportID = getReportId(report);
    if (reportID == null) {
      return new ValidationResult(false, new ReportValidatorException(ERR_NO_REPORT_ID));
    }
    return validateByDefintions(report, reportType);
  }

  public ValidationResult validateReportHeader(ObjectNode report) {
    String reportID = getReportId(report);
    if (reportID == null) {
      return new ValidationResult(false, new ReportValidatorException(ERR_NO_REPORT_ID));
    }
    try {
      return validateByDefintions(report, ReportType.valueOf(reportID));
    } catch (IllegalArgumentException e) {
      return new ValidationResult(
          false,
          new ReportValidatorException(ERR_UNSUPPORTED_REPORT_ID_TEMPLATE.formatted(reportID)));
    }
  }

  private String getReportId(ObjectNode report) {
    String reportID = report.path(REPORT_HEADER).path(REPORT_ID).asText();
    return "".equals(reportID) ? null : reportID;
  }

  private ValidationResult validateByDefintions(ObjectNode report, ReportType reportType) {
    ValidationResult classValidationResult = validateByClassDefinition(report, reportType);
    if (!classValidationResult.isValid()) {
      return classValidationResult;
    }
    ValidationResult headerValidationResult = validateByReportHeaderAttributes(report, reportType);
    if (!headerValidationResult.isValid()) {
      return headerValidationResult;
    }
    return validateByReportHeaderReportAttributesDefinition(report, reportType);
  }

  private ValidationResult validateByClassDefinition(ObjectNode report, ReportType reportType) {
    try {
      objectMapper.convertValue(report.path(REPORT_HEADER), getHeaderClass(reportType));
    } catch (Exception e) {
      return new ValidationResult(false, e);
    }
    return new ValidationResult(true);
  }

  private ValidationResult validateByReportHeaderAttributes(
      ObjectNode report, ReportType reportType) {
    String reportID = reportType.toString();
    if (!report.path(REPORT_HEADER).path(REPORT_ID).asText().equals(reportID)) {
      return new ValidationResult(
          false, new ReportValidatorException(ERR_REPORT_ID_TEMPLATE.formatted(reportID)));
    }
    if (!report.path(REPORT_HEADER).path(JsonProperties.RELEASE).asText().equals(RELEASE)) {
      return new ValidationResult(
          false, new ReportValidatorException(ERR_RELEASE_TEMPLATE.formatted(RELEASE)));
    }
    return new ValidationResult(true);
  }

  private ValidationResult validateByReportHeaderReportAttributesDefinition(
      ObjectNode report, ReportType reportType) {
    JsonNode reportAttributes =
        ofNullable(report.path(REPORT_HEADER).get(REPORT_ATTRIBUTES))
            .orElse(objectMapper.createObjectNode());
    ObjectNode expectedReportAttributes =
        objectMapper.convertValue(
            reportType.getProperties().getReportAttributes(), ObjectNode.class);
    if (reportAttributes.equals(expectedReportAttributes)) {
      return new ValidationResult(true);
    } else {
      return new ValidationResult(
          false,
          new ReportValidatorException(
              ERR_REPORT_ATTRIBUTES_TEMPLATE.formatted(expectedReportAttributes.toString())));
    }
  }

  private Class<?> getHeaderClass(ReportType reportType) throws ClassNotFoundException {
    String reportName = reportType.toString().replace("_", "");
    String headerClassName = HEADER_CLASS_NAME_TEMPLATE.formatted(reportName);
    return Class.forName(headerClassName);
  }

  static class ValidationResult {
    private final boolean isValid;
    private final Throwable error;

    ValidationResult(boolean isValid, Throwable error) {
      this.isValid = isValid;
      this.error = error;
    }

    ValidationResult(boolean isValid) {
      this(isValid, null);
    }

    public boolean isValid() {
      return isValid;
    }

    public String getErrorMessage() {
      return Optional.ofNullable(error)
          .map(
              err -> {
                Throwable rootCause = ExceptionUtils.getRootCause(err);
                return (rootCause != null) ? rootCause.getMessage() : err.getMessage();
              })
          .orElse(null);
    }
  }

  public static class ReportValidatorException extends RuntimeException {

    public ReportValidatorException(String message) {
      super(message);
    }
  }
}
