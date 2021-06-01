package org.olf.erm.usage.counter50.csv.mapper.csv2report;

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
    sushiReportHeader.setCreated(headerColumns.get(SUSHIReportHeader.SERIALIZED_NAME_CREATED));
    sushiReportHeader.setCreatedBy(headerColumns.get(SUSHIReportHeader.SERIALIZED_NAME_CREATED_BY));
    sushiReportHeader.setReportName(
        headerColumns.get(SUSHIReportHeader.SERIALIZED_NAME_REPORT_NAME));
    sushiReportHeader.setReportID(headerColumns.get(SUSHIReportHeader.SERIALIZED_NAME_REPORT_I_D));
    sushiReportHeader.setRelease(headerColumns.get(SUSHIReportHeader.SERIALIZED_NAME_RELEASE));
    sushiReportHeader.setInstitutionName(
        headerColumns.get(SUSHIReportHeader.SERIALIZED_NAME_INSTITUTION_NAME));

    String instID =
        headerColumns.getOrDefault(SUSHIReportHeader.SERIALIZED_NAME_INSTITUTION_I_D, "");
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
    sushiReportHeader.setInstitutionID(orgIdentifiers);

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
    sushiReportHeader.setReportFilters(headerReportFilters);

    String reportAttrs =
        headerColumns.getOrDefault(SUSHIReportHeader.SERIALIZED_NAME_REPORT_ATTRIBUTES, "");
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
    sushiReportHeader.setReportAttributes(headerReportAttributes);

    String errors = headerColumns.getOrDefault(SUSHIReportHeader.SERIALIZED_NAME_EXCEPTIONS, "");
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
                    sushiErrorModel.setSeverity(SeverityEnum.fromValue(split[0]));
                    sushiErrorModel.setCode(Integer.valueOf(split[1]));
                    sushiErrorModel.setMessage(split[2]);

                    if (split.length > 3) {
                      sushiErrorModel.setData(split[3]);
                    }
                    if (split.length > 4) {
                      sushiErrorModel.setHelpURL(split[4]);
                    }
                  }
                  return sushiErrorModel;
                })
            .collect(Collectors.toList());
    sushiReportHeader.setExceptions(errorModels);
    return sushiReportHeader;
  }
}
