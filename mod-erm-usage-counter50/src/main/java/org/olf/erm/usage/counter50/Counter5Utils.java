package org.olf.erm.usage.counter50;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
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

  private Counter5Utils() {}

  public static SUSHIReportHeader getSushiReportHeader(String content)
      throws Counter5UtilsException {
    try {
      JsonObject jsonObject = parser.parse(content).getAsJsonObject();
      return gson.fromJson(jsonObject.getAsJsonObject(REPORT_HEADER), SUSHIReportHeader.class);
    } catch (JsonParseException | IllegalStateException e) {
      throw new Counter5UtilsException(
          String.format("Error parsing SushiReportHeader: %s", e.getMessage()), e);
    }
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
    List<SUSHIReportHeaderReportFilters> reportFilters = header.getReportFilters();

    Optional<YearMonth> beginDate =
        reportFilters.stream()
            .filter(f -> f.getName() != null && f.getName().equals("Begin_Date"))
            .findFirst()
            .map(SUSHIReportHeaderReportFilters::getValue)
            .map(s -> YearMonth.parse(s, DateTimeFormatter.ISO_DATE));
    Optional<YearMonth> endDate =
        reportFilters.stream()
            .filter(f -> f.getName() != null && f.getName().equals("End_Date"))
            .findFirst()
            .map(SUSHIReportHeaderReportFilters::getValue)
            .map(s -> YearMonth.parse(s, DateTimeFormatter.ISO_DATE));

    return beginDate
        .flatMap(
            begin ->
                endDate.map(
                    end -> {
                      if (begin.compareTo(end) > 0) {
                        LOG.warn("Begin_Date > End_Date");
                        return Collections.<YearMonth>emptyList();
                      }
                      return Stream.iterate(begin, next -> next.plusMonths(1))
                          .limit(begin.until(end, ChronoUnit.MONTHS) + 1)
                          .collect(Collectors.toList());
                    }))
        .orElse(Collections.emptyList());
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
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
    String reportID =
        jsonObject.getAsJsonObject(REPORT_HEADER).getAsJsonPrimitive("Report_ID").getAsString();
    if (reportID.startsWith("TR")) {
      result = gson.fromJson(json, COUNTERTitleReport.class);
    } else if (reportID.startsWith("PR")) {
      result = gson.fromJson(json, COUNTERPlatformReport.class);
    } else {
      LOG.error("Cannot cast given json to COUNTER 5 report");
    }
    return result;
  }

  public static class Counter5UtilsException extends Exception {

    public Counter5UtilsException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
