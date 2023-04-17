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

  private static List<SUSHIOrgIdentifiers> parseOrgIdentifiers(String str) {
    String[] splitted = str == null ? new String[0] : str.split(";");
    return Stream.of(splitted)
        .map(
            s -> {
              String[] split = s.split("=");
              SUSHIOrgIdentifiers identifiers = new SUSHIOrgIdentifiers();
              identifiers.setType(TypeEnum.fromValue(split[0].trim()));
              identifiers.setValue(split[1].trim());
              return identifiers;
            })
        .collect(Collectors.toList());
  }

  private static List<SUSHIReportHeaderReportFilters> parseReportHeaderReportFilters(String str) {
    String[] splitted = str == null ? new String[0] : str.split(";");
    return Stream.of(splitted)
        .map(
            s -> {
              String[] split = s.split("=");
              SUSHIReportHeaderReportFilters reportFilters = new SUSHIReportHeaderReportFilters();
              reportFilters.setName(split[0].trim());
              reportFilters.setValue(split[1].trim());
              return reportFilters;
            })
        .collect(Collectors.toList());
  }

  private static List<SUSHIReportHeaderReportAttributes> parseReportHeaderReportAttributes(
      String str) {
    String[] splitted = str == null ? new String[0] : str.split(";");
    return Stream.of(splitted)
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
  }

  private static List<SUSHIErrorModel> parseSUSHIErrorModel(String str) {
    String[] splitted = str == null ? new String[0] : str.split(";");
    return Stream.of(splitted)
        .map(
            s -> {
              String[] split = s.split("-");
              SUSHIErrorModel sushiErrorModel = new SUSHIErrorModel();
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
  }

  public static SUSHIReportHeader parseHeader(Map<String, String> headerColumns) {
    SUSHIReportHeader sushiReportHeader = new SUSHIReportHeader();
    sushiReportHeader.setCreated(headerColumns.get(JSON_PROPERTY_CREATED));
    sushiReportHeader.setCreatedBy(headerColumns.get(JSON_PROPERTY_CREATED_BY));
    sushiReportHeader.setReportName(headerColumns.get(JSON_PROPERTY_REPORT_NAME));
    sushiReportHeader.setReportID(headerColumns.get(JSON_PROPERTY_REPORT_I_D));
    sushiReportHeader.setRelease(headerColumns.get(JSON_PROPERTY_RELEASE));
    sushiReportHeader.setInstitutionName(headerColumns.get(JSON_PROPERTY_INSTITUTION_NAME));

    List<SUSHIOrgIdentifiers> orgIdentifiers =
        parseOrgIdentifiers(headerColumns.get(JSON_PROPERTY_INSTITUTION_I_D));
    if (!orgIdentifiers.isEmpty()) {
      sushiReportHeader.setInstitutionID(orgIdentifiers);
    }

    List<SUSHIReportHeaderReportFilters> reportHeaderReportFilters =
        parseReportHeaderReportFilters(headerColumns.get("Reporting_Period"));
    if (!reportHeaderReportFilters.isEmpty()) {
      sushiReportHeader.setReportFilters(reportHeaderReportFilters);
    }

    List<SUSHIReportHeaderReportAttributes> reportHeaderReportAttributes =
        parseReportHeaderReportAttributes(headerColumns.get(JSON_PROPERTY_REPORT_ATTRIBUTES));
    if (!reportHeaderReportAttributes.isEmpty()) {
      sushiReportHeader.setReportAttributes(reportHeaderReportAttributes);
    }

    List<SUSHIErrorModel> errorModels =
        parseSUSHIErrorModel(headerColumns.get(JSON_PROPERTY_EXCEPTIONS));
    if (!errorModels.isEmpty()) {
      sushiReportHeader.setExceptions(errorModels);
    }

    return sushiReportHeader;
  }
}
