package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import static org.openapitools.client.model.COUNTERItemPerformanceInstance.JSON_PROPERTY_METRIC_TYPE;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_CREATED;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_CREATED_BY;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_EXCEPTIONS;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_INSTITUTION_I_D;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_INSTITUTION_NAME;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_RELEASE;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_REPORT_ATTRIBUTES;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_REPORT_FILTERS;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_REPORT_I_D;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_REPORT_NAME;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openapitools.client.model.SUSHIOrgIdentifiers.TypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

abstract class AbstractReportToCsvMapper<T> implements ReportToCsvMapper {

  public static final String CREATED_PATTERN = "yyyy-MM-dd'T'HH:mm:ssX";
  static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("MMM-uuuu", Locale.ENGLISH);
  private static final String FORMAT_EQUALS = "%s=%s";
  private static final List<String> SUPPORTED_REPORTS =
      List.of("TR", "TR_B1", "TR_B3", "TR_J1", "TR_J3", "TR_J4", "PR", "IR", "DR", "DR_D1");
  private static final List<String> REMOVE_FROM_FILTERS =
      List.of("Metric_Type", "Begin_Date", "End_Date");
  final List<YearMonth> yearMonths;
  final SUSHIReportHeader header;
  final List<String> extendedHeader;
  final List<String> yearMonthsHeader;
  final String[] fullHeader;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  T report;

  AbstractReportToCsvMapper(SUSHIReportHeader header, List<YearMonth> yearMonths) {
    if (SUPPORTED_REPORTS.stream().noneMatch(s -> s.equalsIgnoreCase(header.getReportID()))) {
      throw new IllegalArgumentException(
          "Invalid report type. Possible types are " + String.join(", ", SUPPORTED_REPORTS));
    }
    this.yearMonths = yearMonths;
    this.header = header;
    this.extendedHeader = createExtendedHeader();
    this.yearMonthsHeader =
        yearMonths.stream()
            .map(yearMonth -> yearMonth.format(formatter))
            .collect(Collectors.toList());
    this.fullHeader = createFullHeader();
  }

  abstract String[] getHeader();

  private List<String> createExtendedHeader() {
    List<String> strings = new ArrayList<>(Arrays.asList(getHeader()));
    strings.add("Metric_Type");
    strings.add("Reporting_Period_Total");
    return strings;
  }

  private String getMetricTypes() {
    return this.header.getReportFilters().stream()
        .filter(f -> JSON_PROPERTY_METRIC_TYPE.equals(f.getName()))
        .map(SUSHIReportHeaderReportFilters::getValue)
        .map(s -> s.replace("|", "; "))
        .findFirst()
        .orElse("");
  }

  private CellProcessor[] createProcessors() {
    return Collections.nCopies(fullHeader.length, new Optional()).toArray(CellProcessor[]::new);
  }

  /**
   * Returns Counter ReportItems as Stream of Maps. Each Map represents a row in csv.
   *
   * @param report the Counter Report
   * @return Stream of Map
   */
  abstract Stream<Map<String, Object>> toMapStream(T report);

  private String createReportHeader() {
    StringWriter stringWriter = new StringWriter();
    try (CsvListWriter csvListWriter =
        new CsvListWriter(stringWriter, CsvPreference.STANDARD_PREFERENCE)) {
      csvListWriter.write(JSON_PROPERTY_REPORT_NAME, this.header.getReportName());
      csvListWriter.write(JSON_PROPERTY_REPORT_I_D, this.header.getReportID());
      csvListWriter.write(JSON_PROPERTY_RELEASE, this.header.getRelease());
      csvListWriter.write(JSON_PROPERTY_INSTITUTION_NAME, this.header.getInstitutionName());
      csvListWriter.write(JSON_PROPERTY_INSTITUTION_I_D, getInstitutionId());
      csvListWriter.write("Metric_Types", getMetricTypes());
      csvListWriter.write(JSON_PROPERTY_REPORT_FILTERS, getReportFilters());
      csvListWriter.write(JSON_PROPERTY_REPORT_ATTRIBUTES, getReportAttributes());
      csvListWriter.write(JSON_PROPERTY_EXCEPTIONS, getExceptions());
      String reportingPeriod =
          String.format(
              "Begin_Date=%s; End_Date=%s",
              yearMonths.get(0).atDay(1), Iterables.getLast(yearMonths).atEndOfMonth());
      csvListWriter.write("Reporting_Period", reportingPeriod);
      csvListWriter.write(
          JSON_PROPERTY_CREATED,
          OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(CREATED_PATTERN)));
      csvListWriter.write(JSON_PROPERTY_CREATED_BY, this.header.getCreatedBy());
      csvListWriter.write("");
      csvListWriter.flush();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return "";
    }
    return stringWriter.toString();
  }

  private String getInstitutionId() {
    return java.util.Optional.ofNullable(this.header.getInstitutionID())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            instId -> {
              if (TypeEnum.PROPRIETARY.equals(instId.getType())) {
                return instId.getValue();
              } else {
                return String.join(":", instId.getType().getValue(), instId.getValue());
              }
            })
        .collect(Collectors.joining("; "));
  }

  private String getReportFilters() {
    return this.header.getReportFilters().stream()
        .filter(f -> REMOVE_FROM_FILTERS.stream().noneMatch(s -> s.equals(f.getName())))
        .map(
            reportFilter ->
                String.format(FORMAT_EQUALS, reportFilter.getName(), reportFilter.getValue()))
        .collect(Collectors.joining("; "));
  }

  private String getReportAttributes() {
    return java.util.Optional.ofNullable(this.header.getReportAttributes())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            reportFilter ->
                String.format(FORMAT_EQUALS, reportFilter.getName(), reportFilter.getValue()))
        .collect(Collectors.joining("; "));
  }

  private String getExceptions() {
    return java.util.Optional.ofNullable(this.header.getExceptions())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            e ->
                String.format(
                    "%s - %s - %s - %s - %s",
                    e.getSeverity(), e.getCode(), e.getMessage(), e.getData(), e.getHelpURL()))
        .collect(Collectors.joining("; "))
        .replaceAll("\\R", " ");
  }

  private String[] createFullHeader() {
    return Stream.of(extendedHeader, yearMonthsHeader)
        .flatMap(Collection::stream)
        .toArray(String[]::new);
  }

  private void writeItems(ICsvMapWriter writer) {
    CellProcessor[] processors = createProcessors();

    toMapStream(report)
        .forEach(
            map -> {
              try {
                writer.write(map, fullHeader, processors);
              } catch (IOException e) {
                throw new AbstractReportToCsvMapperException(e);
              }
            });
  }

  public String toCSV() {
    StringWriter stringWriter = new StringWriter();

    stringWriter.append(createReportHeader());

    try (ICsvMapWriter mapWriter =
        new CsvMapWriter(stringWriter, CsvPreference.STANDARD_PREFERENCE)) {
      mapWriter.writeHeader(createFullHeader());

      writeItems(mapWriter);

      mapWriter.flush();
      return stringWriter.toString();

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  static class AbstractReportToCsvMapperException extends RuntimeException {

    public AbstractReportToCsvMapperException(Throwable cause) {
      super(cause);
    }
  }
}
