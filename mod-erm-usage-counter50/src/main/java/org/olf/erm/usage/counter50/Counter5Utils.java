package org.olf.erm.usage.counter50;

import static org.openapitools.client.model.COUNTERTitleReport.JSON_PROPERTY_REPORT_HEADER;
import static org.openapitools.client.model.SUSHIReportHeader.JSON_PROPERTY_REPORT_I_D;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.olf.erm.usage.counter50.csv.mapper.MapperException;
import org.olf.erm.usage.counter50.csv.mapper.MapperFactory;
import org.olf.erm.usage.counter50.merger.MergerFactory;
import org.olf.erm.usage.counter50.merger.ReportsMerger;
import org.olf.erm.usage.counter50.splitter.DRReportsSplitter;
import org.olf.erm.usage.counter50.splitter.IRReportsSplitter;
import org.olf.erm.usage.counter50.splitter.PRReportsSplitter;
import org.olf.erm.usage.counter50.splitter.TRReportsSplitter;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.SUSHIReportHeader;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Counter5Utils {

  private static final Logger LOG = LoggerFactory.getLogger(Counter5Utils.class);

  private static final List<String> PREFIXES = List.of("PR", "DR", "TR", "IR");

  private Counter5Utils() {}

  /**
   * Returns {@link SUSHIReportHeader} from json encoded counter 5 report.
   *
   * @param jsonContent Counter 5 reports, JSON encoded
   * @return {@link SUSHIReportHeader} of given report
   * @throws Counter5UtilsException
   */
  public static SUSHIReportHeader getSushiReportHeader(String jsonContent)
      throws Counter5UtilsException {
    try {
      JsonObject header = new JsonObject(jsonContent).getJsonObject(JSON_PROPERTY_REPORT_HEADER);
      return Json.decodeValue(Json.encode(header), SUSHIReportHeader.class);
    } catch (DecodeException | EncodeException e) {
      throw new Counter5UtilsException(
          String.format("Error parsing SushiReportHeader: %s", e.getMessage()), e);
    }
  }

  /**
   * Returns {@link SUSHIReportHeader} from the given report.
   *
   * @param report Counter 5 reports. Needs to be either a {@link COUNTERDatabaseReport}, {@link
   *     COUNTERItemReport}, {@link COUNTERPlatformReport} or {@link COUNTERTitleReport}
   * @return {@link SUSHIReportHeader} of given report.
   * @throws Counter5UtilsException Throws exception if report is not an instance of classes
   *     specified above.
   */
  public static SUSHIReportHeader getSushiReportHeaderFromReportObject(Object report)
      throws Counter5UtilsException {
    if (report instanceof COUNTERDatabaseReport) {
      return ((COUNTERDatabaseReport) report).getReportHeader();
    } else if (report instanceof COUNTERItemReport) {
      return ((COUNTERItemReport) report).getReportHeader();
    } else if (report instanceof COUNTERPlatformReport) {
      return ((COUNTERPlatformReport) report).getReportHeader();
    } else if (report instanceof COUNTERTitleReport) {
      return ((COUNTERTitleReport) report).getReportHeader();
    } else {
      throw new Counter5UtilsException(
          String.format(
              "Error parsing SushiReportHeader. Report of unknown class: %s",
              report.getClass().toString()));
    }
  }

  /**
   * Checks if supplied {@link SUSHIReportHeader} contains required attributes
   *
   * @param reportHeader {@link SUSHIReportHeader}
   * @return {@code true} if valid, {@code false} otherwise
   */
  public static boolean isValidReportHeader(SUSHIReportHeader reportHeader) {
    return (reportHeader != null
        && reportHeader.getRelease() != null
        && reportHeader.getRelease().equals("5")
        && reportHeader.getReportID() != null
        && PREFIXES.stream().anyMatch(str -> reportHeader.getReportID().startsWith(str)));
  }

  /**
   * Returns {@link List} of {@link YearMonth}s as specified by "Begin_Date" and "End_Date" of given
   * {@link SUSHIReportHeader}
   *
   * @param header The {@link SUSHIReportHeader}
   * @return {@link List} of {@link YearMonth}s starting at given {@link SUSHIReportHeader}'s
   *     "Begin_Date" and ending at its "End_Date"
   */
  public static List<YearMonth> getYearMonthsFromReportHeader(SUSHIReportHeader header) {
    Objects.requireNonNull(header);

    List<LocalDate> localDateFromReportHeader = getLocalDateFromReportHeader(header);
    if (localDateFromReportHeader.isEmpty()) {
      return Collections.emptyList();
    }

    LocalDate beginDate = localDateFromReportHeader.get(0);
    LocalDate endDate = localDateFromReportHeader.get(1);
    if (beginDate.compareTo(endDate) > 0) {
      LOG.warn("Begin_Date > End_Date");
      return Collections.emptyList();
    }

    YearMonth begin = YearMonth.from(beginDate);
    YearMonth end = YearMonth.from(endDate);
    return Stream.iterate(begin, next -> next.plusMonths(1))
        .limit(begin.until(end, ChronoUnit.MONTHS) + 1)
        .collect(Collectors.toList());
  }

  /**
   * Convenience method to get {@link List} of {@link YearMonth}s from given report. For details see
   * {@link #getYearMonthsFromReportHeader(SUSHIReportHeader header)}
   *
   * @param report Counter 5 reports. Needs to be either a {@link COUNTERDatabaseReport}, {@link
   *     COUNTERItemReport}, {@link COUNTERPlatformReport} or {@link COUNTERTitleReport}
   * @return {@link List} of {@link YearMonth}s starting at given {@link SUSHIReportHeader}'s
   *     "Begin_Date" and ending at its "End_Date"
   */
  public static List<YearMonth> getYearMonthFromReport(Object report) {
    if (report instanceof COUNTERDatabaseReport) {
      return getYearMonthsFromReportHeader(((COUNTERDatabaseReport) report).getReportHeader());
    } else if (report instanceof COUNTERItemReport) {
      return getYearMonthsFromReportHeader(((COUNTERItemReport) report).getReportHeader());
    } else if (report instanceof COUNTERPlatformReport) {
      return getYearMonthsFromReportHeader(((COUNTERPlatformReport) report).getReportHeader());
    } else if (report instanceof COUNTERTitleReport) {
      return getYearMonthsFromReportHeader(((COUNTERTitleReport) report).getReportHeader());
    }
    return Collections.emptyList();
  }

  /**
   * Transforms "Begin_Date" and "End_Date" of given {@link SUSHIReportHeader} into {@link
   * LocalDate}s.
   *
   * @param header {@link SUSHIReportHeader}
   * @return {@link List} of {@link LocalDate}s containing the header's "Begin_Date" and "End_Date".
   *     Returns empty list if dates are not present.
   */
  public static List<LocalDate> getLocalDateFromReportHeader(SUSHIReportHeader header) {
    Objects.requireNonNull(header);
    List<SUSHIReportHeaderReportFilters> reportFilters = header.getReportFilters();

    Optional<LocalDate> beginDate =
        reportFilters.stream()
            .filter(f -> f.getName() != null && f.getName().trim().equalsIgnoreCase("begin_date"))
            .findFirst()
            .map(SUSHIReportHeaderReportFilters::getValue)
            .map(s -> LocalDate.parse(s, DateTimeFormatter.ISO_DATE));
    Optional<LocalDate> endDate =
        reportFilters.stream()
            .filter(f -> f.getName() != null && f.getName().trim().equalsIgnoreCase("end_date"))
            .findFirst()
            .map(SUSHIReportHeaderReportFilters::getValue)
            .map(s -> LocalDate.parse(s, DateTimeFormatter.ISO_DATE));
    if (beginDate.isPresent() && endDate.isPresent()) {
      return Arrays.asList(beginDate.get(), endDate.get());
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Transforms and returns given Counter 5 report as a csv
   *
   * @param report Counter 5 report. Needs to be either a {@link COUNTERDatabaseReport}, {@link
   *     COUNTERItemReport}, {@link COUNTERPlatformReport} or {@link COUNTERTitleReport}
   * @return CSV representation of given report.
   */
  public static String toCSV(Object report) {
    try {
      return MapperFactory.createReportToCsvMapper(report).toCSV();
    } catch (MapperException e) {
      LOG.error("Error mapping from Report to CSV: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Creates an object representation of given report encoded in json
   *
   * @param json JSON encoded report
   * @return {@link Object} representation of given report. It is an instance of either {@link
   *     COUNTERDatabaseReport}, {@link COUNTERItemReport}, {@link COUNTERPlatformReport} or {@link
   *     COUNTERTitleReport}.
   * @throws Counter5UtilsException Throws exception if report cannot be merged to an instance of
   *     classes specified above.
   */
  public static Object fromJSON(String json) throws Counter5UtilsException {
    Object result = null;
    try {
      JsonObject jsonObject = new JsonObject(json);
      String reportID =
          jsonObject
              .getJsonObject(JSON_PROPERTY_REPORT_HEADER)
              .getString(JSON_PROPERTY_REPORT_I_D)
              .toUpperCase();
      if (reportID.startsWith("TR")) {
        result = Json.decodeValue(json, COUNTERTitleReport.class);
      } else if (reportID.startsWith("PR")) {
        result = Json.decodeValue(json, COUNTERPlatformReport.class);
      } else if (reportID.startsWith("IR")) {
        result = Json.decodeValue(json, COUNTERItemReport.class);
      } else if (reportID.startsWith("DR")) {
        result = Json.decodeValue(json, COUNTERDatabaseReport.class);
      } else {
        throw new Counter5UtilsException(
            String.format("Error converting report. Unknown report type: %s", reportID));
      }
    } catch (DecodeException | Counter5UtilsException e) {
      LOG.error(e.getMessage(), e);
    }
    return result;
  }

  /**
   * Creates an object representation of given report encoded in csv
   *
   * @param csv CSV encoded report
   * @return {@link Object} representation of given report. It is an instance of either {@link
   *     COUNTERDatabaseReport}, {@link COUNTERItemReport}, {@link COUNTERPlatformReport} or {@link
   *     COUNTERTitleReport}.
   */
  public static Object fromCSV(String csv) throws MapperException {
    return MapperFactory.createCsvToReportMapper(csv).toReport();
  }

  /**
   * Merges COUNTER 5 reports of several months into one report. Input reports must be of same type.
   *
   * @param reports Valid types are {@link COUNTERDatabaseReport}, {@link COUNTERTitleReport},
   *     {@link COUNTERPlatformReport} & {@link COUNTERItemReport}.
   * @param <T> Valid types are {@link COUNTERDatabaseReport}, {@link COUNTERTitleReport}, {@link
   *     COUNTERPlatformReport} & {@link COUNTERItemReport}.
   * @return The merged report.
   * @throws Counter5UtilsException Throws exception if reports cannot be merged (e.g. if reports
   *     are not of the same class).
   */
  public static <T> T merge(List<T> reports) throws Counter5UtilsException {
    boolean allObjectsSameClass =
        reports.stream().map(Object::getClass).distinct().limit(2).count() <= 1;
    if (!allObjectsSameClass) {
      throw new Counter5UtilsException("Cannot merge reports. Reports not of same class.");
    }
    //noinspection unchecked
    ReportsMerger<T> merger = MergerFactory.createMerger(reports.get(0));
    return merger.merge(reports);
  }

  /**
   * Splits a COUNTER 5 report into reports of several months.
   *
   * @param report Valid types are {@link COUNTERDatabaseReport}, {@link COUNTERTitleReport}, {@link
   *     COUNTERPlatformReport} & {@link COUNTERItemReport}.
   * @return {@link List} of splitted reports.
   * @throws Counter5UtilsException Throws exception if report is not an instance of classes
   *     specified above.
   */
  // Cannot use parameterized type as the generated report types only have Object as common
  // super-type.
  @SuppressWarnings({"rawtypes", "java:S3740"})
  public static List split(Object report) throws Counter5UtilsException {
    if (report instanceof COUNTERDatabaseReport) {
      DRReportsSplitter splitter = new DRReportsSplitter();
      return splitter.split((COUNTERDatabaseReport) report);
    } else if (report instanceof COUNTERItemReport) {
      IRReportsSplitter splitter = new IRReportsSplitter();
      return splitter.split((COUNTERItemReport) report);
    } else if (report instanceof COUNTERPlatformReport) {
      PRReportsSplitter splitter = new PRReportsSplitter();
      return splitter.split((COUNTERPlatformReport) report);
    } else if (report instanceof COUNTERTitleReport) {
      TRReportsSplitter splitter = new TRReportsSplitter();
      return splitter.split((COUNTERTitleReport) report);
    } else {
      throw new Counter5UtilsException(
          String.format(
              "Error splitting report. Report of unknown class: %s", report.getClass().toString()));
    }
  }

  public static class Counter5UtilsException extends Exception {

    public Counter5UtilsException(String message, Throwable cause) {
      super(message, cause);
    }

    public Counter5UtilsException(String message) {
      super(message);
    }
  }
}
