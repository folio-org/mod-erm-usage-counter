package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_CREATED;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_CREATED_BY;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_EXCEPTIONS;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_INSTITUTION_I_D;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_INSTITUTION_NAME;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_RELEASE;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_REPORT_ATTRIBUTES;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_REPORT_I_D;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_REPORT_NAME;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openapitools.client.model.SUSHIErrorModel;
import org.openapitools.client.model.SUSHIErrorModel.SeverityEnum;
import org.openapitools.client.model.SUSHIOrgIdentifiers;
import org.openapitools.client.model.SUSHIOrgIdentifiers.TypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;
import org.openapitools.client.model.SUSHIReportHeaderReportAttributes;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvHeaderToReportHeader {

  private static final Logger log = LoggerFactory.getLogger(CsvHeaderToReportHeader.class);

  private CsvHeaderToReportHeader() {}

  public static SUSHIReportHeader parseHeader(Map<String, String> headerColumns) {
    SUSHIReportHeader sushiReportHeader = new SUSHIReportHeader();
    sushiReportHeader.setCreated(headerColumns.get(JSON_PROPERTY_CREATED));
    sushiReportHeader.setCreatedBy(headerColumns.get(JSON_PROPERTY_CREATED_BY));
    sushiReportHeader.setReportName(headerColumns.get(JSON_PROPERTY_REPORT_NAME));
    sushiReportHeader.setReportID(headerColumns.get(JSON_PROPERTY_REPORT_I_D));
    sushiReportHeader.setRelease(headerColumns.get(JSON_PROPERTY_RELEASE));
    sushiReportHeader.setInstitutionName(headerColumns.get(JSON_PROPERTY_INSTITUTION_NAME));

    String instID = headerColumns.getOrDefault(JSON_PROPERTY_INSTITUTION_I_D, "");
    String[] splittedInstID = instID == null ? new String[0] : instID.split(";");
    List<SUSHIOrgIdentifiers> orgIdentifiers =
        Stream.of(splittedInstID)
            .map(
                s -> {
                  String[] split = s.split("=");
                  SUSHIOrgIdentifiers identifiers = new SUSHIOrgIdentifiers();
                  identifiers.setType(TypeEnum.fromValue(split[0]));
                  identifiers.setValue(split[1]);
                  return identifiers;
                })
            .collect(Collectors.toList());
    sushiReportHeader.setInstitutionID((orgIdentifiers.isEmpty()) ? null : orgIdentifiers);

    String repPeriod = headerColumns.getOrDefault("Reporting_Period", "");
    String[] splittedRepPeriod = repPeriod == null ? new String[0] : repPeriod.split(";");
    List<SUSHIReportHeaderReportFilters> headerReportFilters =
        Stream.of(splittedRepPeriod)
            .map(
                s -> {
                  String[] split = s.split("=");
                  SUSHIReportHeaderReportFilters reportFilters =
                      new SUSHIReportHeaderReportFilters();
                  reportFilters.setName(split[0].trim());
                  reportFilters.setValue(split[1].trim());
                  return reportFilters;
                })
            .collect(Collectors.toList());
    sushiReportHeader.setReportFilters(
        (headerReportFilters.isEmpty()) ? null : headerReportFilters);

    String reportAttrs = headerColumns.getOrDefault(JSON_PROPERTY_REPORT_ATTRIBUTES, "");
    String[] splitted = reportAttrs == null ? new String[0] : reportAttrs.split(";");
    List<SUSHIReportHeaderReportAttributes> headerReportAttributes =
        Stream.of(splitted)
            .map(
                s -> {
                  String[] split = s.split("=");
                  SUSHIReportHeaderReportAttributes attributes =
                      new SUSHIReportHeaderReportAttributes();
                  attributes.setName(split[0].trim());
                  attributes.setValue(split[1].trim());
                  return attributes;
                })
            .collect(Collectors.toList());
    sushiReportHeader.setReportAttributes(
        (headerReportAttributes.isEmpty()) ? null : headerReportAttributes);

    String errors = headerColumns.getOrDefault(JSON_PROPERTY_EXCEPTIONS, "");
    String[] splittedErrors = errors == null ? new String[0] : errors.split(";");
    List<SUSHIErrorModel> errorModels =
        Stream.of(splittedErrors)
            .map(
                s -> {
                  SUSHIErrorModel sushiErrorModel = new SUSHIErrorModel();
                  String[] split = s.split("-");
                  if (split.length < 3) {
                    log.error(
                        String.format(
                            "Exception needs to have at least 3 entries: code, severity, message. Got exception: %s",
                            s));
                  } else {
                    sushiErrorModel.setSeverity(SeverityEnum.fromValue(split[0].trim()));
                    sushiErrorModel.setCode(Integer.valueOf(split[1].trim()));
                    sushiErrorModel.setMessage(split[2].trim());

                    if (split.length > 3 && !split[3].trim().equals("null")) {
                      sushiErrorModel.setData(split[3].trim());
                    }
                    if (split.length > 4 && !split[4].trim().equals("null")) {
                      sushiErrorModel.setHelpURL(split[4].trim());
                    }
                  }
                  return sushiErrorModel;
                })
            .collect(Collectors.toList());
    sushiReportHeader.setExceptions((errorModels.isEmpty()) ? null : errorModels);
    return sushiReportHeader;
  }
}
