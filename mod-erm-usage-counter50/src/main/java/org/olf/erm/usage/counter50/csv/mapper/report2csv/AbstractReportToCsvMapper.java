package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openapitools.client.model.SUSHIReportHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

public abstract class AbstractReportToCsvMapper<T> implements ReportToCsvMapper {

  protected static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("MMM-uuuu", Locale.ENGLISH);
  private static final String FORMAT_EQUALS = "%s=%s";
  private static final List<String> SUPPORTED_REPORTS =
      List.of("TR", "TR_B1", "TR_B3", "TR_J1", "TR_J3", "TR_J4", "PR", "IR", "DR", "DR_D1");
  protected final List<YearMonth> yearMonths;
  protected final SUSHIReportHeader header;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  AbstractReportToCsvMapper(SUSHIReportHeader header, List<YearMonth> yearMonths) {
    if (SUPPORTED_REPORTS.stream().noneMatch(s -> s.equalsIgnoreCase(header.getReportID()))) {
      throw new IllegalArgumentException(
          "Invalid report type. Possible types are " + String.join(", ", SUPPORTED_REPORTS));
    }
    this.yearMonths = yearMonths;
    this.header = header;
  }

  protected abstract String[] getHeader();

  protected abstract T getReport();

  protected abstract String getMetricTypes();

  protected abstract CellProcessor[] createProcessors();

  protected abstract List<Map<String, Object>> toMap(T report);

  protected String createReportHeader() {
    StringWriter stringWriter = new StringWriter();
    try (CsvListWriter csvListWriter =
        new CsvListWriter(stringWriter, CsvPreference.STANDARD_PREFERENCE)) {
      csvListWriter.write(
          SUSHIReportHeader.SERIALIZED_NAME_REPORT_NAME, this.header.getReportName());
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_REPORT_I_D, this.header.getReportID());
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_RELEASE, this.header.getRelease());
      csvListWriter.write(
          SUSHIReportHeader.SERIALIZED_NAME_INSTITUTION_NAME, this.header.getInstitutionName());
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_INSTITUTION_I_D, getInstitutionId());
      csvListWriter.write("Metric_Types", getMetricTypes());
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_REPORT_FILTERS, getReportFilters());
      csvListWriter.write(
          SUSHIReportHeader.SERIALIZED_NAME_REPORT_ATTRIBUTES, getReportAttributes());
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_EXCEPTIONS, getExceptions());
      String reportingPeriod =
          String.format(
              "Begin_Date=%s; End_Date=%s",
              yearMonths.get(0).atDay(1), Iterables.getLast(yearMonths).atEndOfMonth());
      csvListWriter.write("Reporting_Period", reportingPeriod);
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_CREATED, LocalDate.now().toString());
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_CREATED_BY, this.header.getCreatedBy());
      csvListWriter.write("");
      csvListWriter.flush();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return "";
    }
    return stringWriter.toString();
  }

  private String getInstitutionId() {
    return this.header.getInstitutionID().stream()
        .map(instId -> String.format(FORMAT_EQUALS, instId.getType(), instId.getValue()))
        .collect(Collectors.joining("; "));
  }

  // TODO: remove metric type, begin_date, end_date from filters as they have a separate row
  private String getReportFilters() {
    return this.header.getReportFilters().stream()
        .map(
            reportFilter ->
                String.format(FORMAT_EQUALS, reportFilter.getName(), reportFilter.getValue()))
        .collect(Collectors.joining("; "));
  }

  private String getReportAttributes() {
    return this.header.getReportAttributes().stream()
        .map(
            reportFilter ->
                String.format(FORMAT_EQUALS, reportFilter.getName(), reportFilter.getValue()))
        .collect(Collectors.joining("; "));
  }

  private String getExceptions() {
    return this.header.getExceptions().stream()
        .map(
            e ->
                String.format(
                    "%s - %s - %s - %s - %s",
                    e.getSeverity(), e.getCode(), e.getMessage(), e.getData(), e.getHelpURL()))
        .collect(Collectors.joining("; "));
  }

  List<YearMonth> getYearMonths() {
    return yearMonths;
  }

  private String[] createHeader() {
    Stream<String> h = Arrays.stream(getHeader());
    Stream<String> months = yearMonths.stream().map(ym -> ym.format(formatter));
    return Stream.concat(h, months).toArray(String[]::new);
  }

  private void writeItems(ICsvMapWriter writer) throws IOException {
    CellProcessor[] processors = createProcessors();

    List<String> ym =
        getYearMonths().stream()
            .map(yearMonth -> yearMonth.format(formatter))
            .collect(Collectors.toList());
    List<String> h = Arrays.asList(getHeader());
    List<String> headerList =
        Stream.of(h, ym).flatMap(Collection::stream).collect(Collectors.toList());

    String[] headerArray = new String[headerList.size()];
    headerArray = headerList.toArray(headerArray);

    List<Map<String, Object>> entries = toMap(getReport());
    for (final Map<String, Object> item : entries) {
      writer.write(item, headerArray, processors);
    }
  }

  public String toCSV() {
    StringWriter stringWriter = new StringWriter();

    stringWriter.append(createReportHeader());

    try (ICsvMapWriter mapWriter =
        new CsvMapWriter(stringWriter, CsvPreference.STANDARD_PREFERENCE)) {
      mapWriter.writeHeader(createHeader());

      writeItems(mapWriter);

      mapWriter.flush();
      return stringWriter.toString();

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }
}
