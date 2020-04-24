package org.olf.erm.usage.counter50;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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

  private static final List<String> PREFIXES =
      Collections.unmodifiableList(Arrays.asList("PR", "DR", "TR", "IR"));
  private static final Gson gson = new Gson();
  private static final JsonParser parser = new JsonParser();
  private static final String REPORT_HEADER = "Report_Header";

  private Counter5Utils() {
  }

  public static SUSHIReportHeader getSushiReportHeader(String jsonContent)
      throws Counter5UtilsException {
    try {
      JsonObject jsonObject = parser.parse(jsonContent).getAsJsonObject();
      return gson.fromJson(jsonObject.getAsJsonObject(REPORT_HEADER), SUSHIReportHeader.class);
    } catch (JsonParseException | IllegalStateException e) {
      throw new Counter5UtilsException(
          String.format("Error parsing SushiReportHeader: %s", e.getMessage()), e);
    }
  }

  public static SUSHIReportHeader getSushiReportHeaderFromReportObject(Object report) {
    if (report instanceof COUNTERDatabaseReport) {
      return ((COUNTERDatabaseReport) report).getReportHeader();
    } else if (report instanceof COUNTERItemReport) {
      return ((COUNTERItemReport) report).getReportHeader();
    } else if (report instanceof COUNTERPlatformReport) {
      return ((COUNTERPlatformReport) report).getReportHeader();
    } else if (report instanceof COUNTERTitleReport) {
      return ((COUNTERTitleReport) report).getReportHeader();
    }
    return null;
  }

  /**
   * @param content
   * @return
   * @deprecated As of 1.3.0, use {@link #getSushiReportHeader(String)} instead
   */
  @Deprecated
  public static SUSHIReportHeader getReportHeader(String content) {
    SUSHIReportHeader reportHeader;
    try {
      JsonObject jsonObject = parser.parse(content).getAsJsonObject();
      reportHeader =
          gson.fromJson(jsonObject.getAsJsonObject(REPORT_HEADER), SUSHIReportHeader.class);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
    return reportHeader;
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

  public static String toCSV(Object report) {
    try {
      return MapperFactory.createCSVMapper(report).toCSV();
    } catch (MapperException e) {
      LOG.error("Error mapping from Report to CSV: {}", e.getMessage());
      return null;
    }
  }

  public static Object fromJSON(String json) {
    Object result = null;
    try {
      Gson gson = new Gson();
      JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
      String reportID =
          jsonObject.getAsJsonObject(REPORT_HEADER).getAsJsonPrimitive("Report_ID").getAsString()
              .toUpperCase();
      if (reportID.startsWith("TR")) {
        result = gson.fromJson(json, COUNTERTitleReport.class);
      } else if (reportID.startsWith("PR")) {
        result = gson.fromJson(json, COUNTERPlatformReport.class);
      } else if (reportID.startsWith("IR")) {
        result = gson.fromJson(json, COUNTERItemReport.class);
      } else if (reportID.startsWith("DR")) {
        result = gson.fromJson(json, COUNTERDatabaseReport.class);
      } else {
        LOG.error("Cannot cast given json to COUNTER 5 report");
      }
    } catch (JsonSyntaxException e) {
      LOG.error(e.getMessage(), e);
    }
    return result;
  }

  public static Object fromCSV(String csv) throws MapperException {
    return MapperFactory.createCsvToReportMapper(csv).toReport();
  }

  /**
   * Merges COUNTER 5 reports of several months into one report. Input reports must be of same type.
   * Valid types are {@link COUNTERTitleReport}, {@link COUNTERPlatformReport} & {@link
   * COUNTERItemReport}. {@link COUNTERDatabaseReport} is currently not supported.
   *
   * @param reports
   * @param <T>
   * @return
   * @throws Counter5UtilsException
   */
  public static <T> T merge(List<T> reports) throws Counter5UtilsException {
    boolean allObjectsSameClass =
        reports.stream().map(Object::getClass).distinct().limit(2).count() <= 1;
    if (!allObjectsSameClass) {
      throw new Counter5UtilsException("Cannot merge reports. Reports not of same class.");
    }
    ReportsMerger<T> merger = MergerFactory.createMerger(reports.get(0));
    return merger.merge(reports);
  }

  public static List split(Object report) {
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
    }
    return Collections.emptyList();
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
