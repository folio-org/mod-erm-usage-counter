package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.StringWriter;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;
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
  protected final T report;
  protected final List<YearMonth> yearMonths;
  protected final SUSHIReportHeader header;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  AbstractReportToCsvMapper(T report, List<YearMonth> yearMonths) {
    if (!(report instanceof COUNTERTitleReport) && !(report instanceof COUNTERPlatformReport)) {
      throw new IllegalArgumentException(
          "Invalid report type. Possible types are COUNTERTitleReport and COUNTERPlatformReport");
    }
    this.report = report;
    this.yearMonths = yearMonths;
    if (report instanceof COUNTERTitleReport) {
      this.header = getHeader((COUNTERTitleReport) report);
    } else if (report instanceof COUNTERPlatformReport) {
      this.header = getHeader((COUNTERPlatformReport) report);
    } else {
      header = null;
    }
  }

  SUSHIReportHeader getHeader(IGetHeader parameter) {
    return parameter.getHeader();
  }

  SUSHIReportHeader getHeader(COUNTERTitleReport parameter) {
    return getHeader(parameter::getReportHeader);
  }

  SUSHIReportHeader getHeader(COUNTERPlatformReport parameter) {
    return getHeader(parameter::getReportHeader);
  }

  protected abstract String[] getHeader();

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
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_EXCEPTIONS, "");
      String reportingPeriod =
          String.format(
              "Begin_Date=%s; End_Date=%s",
              yearMonths.get(0).atDay(1).toString(),
              Iterables.getLast(yearMonths).atEndOfMonth().toString());
      csvListWriter.write("Reporting_Period", reportingPeriod);
      csvListWriter.write(SUSHIReportHeader.SERIALIZED_NAME_CREATED, this.header.getCreated());
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

  protected abstract String getMetricTypes();

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

  List<YearMonth> getYearMonths() {
    return yearMonths;
  }

  private String[] createHeader() {
    Stream<String> h = Arrays.stream(getHeader());
    Stream<String> months = yearMonths.stream().map(ym -> ym.format(formatter));
    return Stream.concat(h, months).toArray(String[]::new);
  }

  protected abstract CellProcessor[] createProcessors();

  protected abstract List<Map<String, Object>> toMap(T report);

  private void writeItems(ICsvMapWriter writer) throws IOException {
    CellProcessor[] processors = createProcessors();
    List<Map<String, Object>> entries = toMap(report);
    List<String> h = Arrays.asList(getHeader());
    List<String> ym =
        getYearMonths().stream()
            .map(yearMonth -> yearMonth.format(formatter))
            .collect(Collectors.toList());
    List<String> headerList =
        Stream.of(h, ym).flatMap(Collection::stream).collect(Collectors.toList());
    String[] headerArray = new String[headerList.size()];
    headerArray = headerList.toArray(headerArray);
    for (final Map<String, Object> item : entries) {
      writer.write(item, headerArray, processors);
    }
  }

  public String toCSV() {
    StringWriter stringWriter = new StringWriter();

    stringWriter.append(createReportHeader());

    try (ICsvMapWriter mapWriter =
        new CsvMapWriter(stringWriter, CsvPreference.STANDARD_PREFERENCE)) {

      // write the header
      mapWriter.writeHeader(createHeader());

      writeItems(mapWriter);

      mapWriter.flush();
      return stringWriter.toString();

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  interface IGetHeader {
    SUSHIReportHeader getHeader();
  }
}
