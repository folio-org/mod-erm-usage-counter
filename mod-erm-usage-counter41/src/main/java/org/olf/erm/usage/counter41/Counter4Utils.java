package org.olf.erm.usage.counter41;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Map.entry;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXB;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.lang3.SerializationUtils;
import org.niso.schemas.counter.DateRange;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.niso.schemas.sushi.Exception;
import org.niso.schemas.sushi.ExceptionSeverity;
import org.niso.schemas.sushi.counter.CounterReportResponse;
import org.olf.erm.usage.counter41.csv.mapper.MapperException;
import org.olf.erm.usage.counter41.csv.mapper.MapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Counter4Utils {

  public static final ObjectMapper mapper = createObjectMapper();
  private static final Map<String, String> mappingEntries =
      Map.ofEntries(
          entry("JR1", "(?:JR1|Journal Report 1)(?! GOA)(?: \\(R4\\))?"),
          entry("JR2", "(?:JR2|Journal Report 2)(?: \\(R4\\))?"),
          entry("JR3", "(?:JR3|Journal Report 3)(?: \\(R4\\))?"),
          entry("JR4", "(?:JR4|Journal Report 4)(?: \\(R4\\))?"),
          entry("JR5", "(?:JR5|Journal Report 5)(?: \\(R4\\))?"),
          entry("JR1 GOA", "(?:JR1|Journal Report 1) GOA(?: \\(R4\\))?"),
          entry("JR1a", "(?:JR1a|Journal Report 1a)(?: \\(R4\\))?"),
          entry("JR3 Mobile", "(?:JR3|Journal Report 3) Mobile(?: \\(R4\\))?"),
          entry("BR1", "(?:BR1|Book Report 1)(?: \\(R4\\))?"),
          entry("BR2", "(?:BR2|Book Report 2)(?: \\(R4\\))?"),
          entry("BR3", "(?:BR3|Book Report 3)(?: \\(R4\\))?"),
          entry("BR4", "(?:BR4|Book Report 4)(?: \\(R4\\))?"),
          entry("BR5", "(?:BR5|Book Report 5)(?: \\(R4\\))?"),
          entry("BR7", "(?:BR7|Book Report 7)(?: \\(R4\\))?"),
          entry("DB1", "(?:DB1|Database Report 1)(?: \\(R4\\))?"),
          entry("DB2", "(?:DB2|Database Report 2)(?: \\(R4\\))?"),
          entry("MM1", "(?:MM1|Multimedia Report 1)(?: \\(R4\\))?"),
          entry("MM2", "(?:MM2|Multimedia Report 2)(?: \\(R4\\))?"),
          entry("TR1", "(?:TR1|Title Report 1)(?: \\(R4\\))?"),
          entry("TR2", "(?:TR2|Title Report 2)(?: \\(R4\\))?"),
          entry("TR3", "(?:TR3|Title Report 3)(?: \\(R4\\))?"),
          entry("TR1 Mobile", "(?:TR1|Title Report 1) Mobile(?: \\(R4\\))?"),
          entry("TR3 Mobile", "(?:TR3|Title Report 3) Mobile(?: \\(R4\\))?"),
          entry("PR1", "(?:PR1|Platform Report 1)(?: \\(R4\\))?"));

  private static final Logger log = LoggerFactory.getLogger(Counter4Utils.class);

  private Counter4Utils() {}

  /**
   * @deprecated
   */
  @Deprecated(since = "2.2.6", forRemoval = true)
  public static List<String> getTitlesForReportName(String reportName) {
    String result = mappingEntries.get(reportName);
    return (result == null) ? null : List.of(result);
  }

  public static String getNameForReportTitle(String title) {
    return (title == null)
        ? null
        : mappingEntries.entrySet().stream()
            .filter(e -> title.matches(e.getValue()))
            .map(Entry::getKey)
            .findFirst()
            .orElse(null);
  }

  public static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(new XMLGregorianCalendarSerializer());
    module.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
    mapper.registerModule(module);
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  public static String toJSON(Report report) {
    String str = null;
    try {
      str = mapper.writeValueAsString(report);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
    return str;
  }

  public static String toXML(Report report) {
    StringWriter sw = new StringWriter();
    try {
      JAXB.marshal(report, sw);
    } catch (java.lang.Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
    return sw.toString();
  }

  public static String toXML(String report) {
    return Optional.ofNullable(fromJSON(report)).map(Counter4Utils::toXML).orElse(null);
  }

  public static Report fromJSON(String json) {
    Report result = null;
    try {
      result = mapper.readValue(json, Report.class);
    } catch (java.lang.Exception e) {
      log.error(e.getMessage(), e);
    }
    return result;
  }

  public static String toCSV(Report report) {
    try {
      return MapperFactory.createCSVMapper(report).toCSV();
    } catch (MapperException e) {
      log.error("Error mapping from Report to CSV: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Creates a {@link Report} object from a csv string representation.
   *
   * @param csvString report data as csv string
   * @return {@link Report}
   */
  public static Report fromCsvString(String csvString) throws MapperException, IOException {
    return MapperFactory.createCsvToReportMapper(csvString).toReport();
  }

  public static Report fromString(String content) {
    try {
      CounterReportResponse crr =
          JAXB.unmarshal(new StringReader(content), CounterReportResponse.class);
      return crr.getReport().getReport().get(0);
    } catch (java.lang.Exception e) {
      try {
        return JAXB.unmarshal(new StringReader(content), Report.class);
      } catch (java.lang.Exception e1) {
        return null;
      }
    }
  }

  public static List<Exception> getExceptions(CounterReportResponse response) {
    return response.getException().stream()
        .filter(
            e ->
                e.getSeverity().equals(ExceptionSeverity.ERROR)
                    || e.getSeverity().equals(ExceptionSeverity.FATAL))
        .collect(Collectors.toList());
  }

  public static String getErrorMessages(List<Exception> exs) {
    return exs.stream()
        .map(
            e -> {
              String data = null;
              if (e.getData() != null && e.getData().getValue() instanceof Element) {
                Node n = ((Element) e.getData().getValue()).getFirstChild();
                if (n != null && !n.getTextContent().isEmpty()) data = n.getTextContent();
              }
              String helpUrl =
                  (e.getHelpUrl() == null || Strings.isNullOrEmpty(e.getHelpUrl().getValue()))
                      ? null
                      : e.getHelpUrl().getValue();
              return toStringHelper(e)
                  .add("Number", e.getNumber())
                  .add("Severity", e.getSeverity())
                  .add("Message", e.getMessage())
                  .add("HelpUrl", helpUrl)
                  .add("Data", data)
                  .omitNullValues()
                  .toString();
            })
        .collect(Collectors.joining(", "));
  }

  public static List<YearMonth> getYearMonthsFromReport(Report report) {
    return report.getCustomer().get(0).getReportItems().stream()
        .flatMap(ri -> ri.getItemPerformance().stream())
        .flatMap(
            m ->
                Stream.of(
                    YearMonth.of(
                        m.getPeriod().getBegin().getYear(), m.getPeriod().getBegin().getMonth()),
                    YearMonth.of(
                        m.getPeriod().getEnd().getYear(), m.getPeriod().getEnd().getMonth())))
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  /** Same as {@link Counter4Utils#merge(Report...)} */
  public static Report merge(Collection<Report> c) throws ReportMergeException {
    return merge(c.toArray(new Report[0]));
  }

  /**
   * Merges multiple {@link Report} objects into a single {@link Report}.
   *
   * @param reports varArgs of {@link Report}
   * @return {@link Report}
   */
  public static Report merge(Report... reports) throws ReportMergeException {
    Report[] clonedReports = SerializationUtils.clone(reports);

    if (!Stream.of(clonedReports).allMatch(r -> r.getCustomer().size() == 1)) {
      throw new ReportMergeException(
          "At least one report contains invalid customer definitions (expecting one customer per report)");
    }

    // check that provided reports have the same attributes
    if (Stream.of(clonedReports)
            .map(
                r -> {
                  // reset some attributes for equals() check
                  r.getCustomer().get(0).getReportItems().clear();
                  r.setVendor(null);
                  r.setCreated(null);
                  r.setID(null);
                  return r;
                })
            .distinct()
            .count()
        != 1) throw new ReportMergeException("Report attributes do not match");

    List<ReportItem> sortedCombinedReportItems =
        Stream.of(reports)
            .flatMap(r -> r.getCustomer().get(0).getReportItems().stream())
            .collect(
                Collectors.toMap(
                    ReportItem::getItemIdentifier,
                    ri -> ri,
                    (a, b) -> {
                      a.getItemPerformance().addAll(b.getItemPerformance());
                      return a;
                    }))
            .values()
            .stream()
            .sorted(Comparator.comparing(ReportItem::getItemName))
            .collect(Collectors.toList());

    clonedReports[0].getCustomer().get(0).getReportItems().addAll(sortedCombinedReportItems);
    return clonedReports[0];
  }

  /**
   * Splits a report that spans multiple months into a list of reports spanning one month each
   *
   * @param report Report with multiple months
   * @return List of Reports with one month only
   */
  public static List<Report> split(Report report) throws ReportSplitException {
    if (report.getCustomer().isEmpty()) {
      throw new ReportSplitException("Report contains no customer");
    }
    if (report.getCustomer().size() > 1) {
      throw new ReportSplitException("Report contains multiple customer entries");
    }

    List<YearMonth> yearMonths = getYearMonthsFromReport(report);
    ArrayList<Report> resultList = new ArrayList<>();
    yearMonths.forEach(
        ym -> {
          Report clone = SerializationUtils.clone(report);
          DateRange dateRange = new DateRange();
          dateRange.setBegin(ym.atDay(1));
          dateRange.setEnd(ym.atEndOfMonth());

          List<ReportItem> reportItems = clone.getCustomer().get(0).getReportItems();
          reportItems.removeIf(
              ri ->
                  ri.getItemPerformance().stream()
                      .map(Metric::getPeriod)
                      .noneMatch(dr -> dr.equals(dateRange)));

          reportItems.stream()
              .map(ReportItem::getItemPerformance)
              .forEach(list -> list.removeIf(metric -> !metric.getPeriod().equals(dateRange)));

          resultList.add(clone);
        });
    return resultList;
  }

  @Deprecated(forRemoval = true, since = "3.1.0")
  /**
   * Converts a {@link Temporal} into {@link XMLGregorianCalendar}.
   *
   * @param temporal {@link Temporal}
   * @return {@link XMLGregorianCalendar}
   */
  public static XMLGregorianCalendar toXMLGregorianCalendar(Temporal temporal) {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(temporal.toString());
    } catch (java.lang.Exception e) {
      log.error("Error creating XMLGregorianCalendar from Temporal: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * Creates a {@link DateRange} object with begin at first and end at last day from supplied {@link
   * YearMonth}.
   *
   * @param yearMonth {@link YearMonth}
   * @return {@link DateRange} or {@code null} on error
   */
  public static DateRange getDateRangeForYearMonth(YearMonth yearMonth) {
    DateRange dateRange = new DateRange();
    dateRange.setBegin(yearMonth.atDay(1));
    dateRange.setEnd(yearMonth.atEndOfMonth());
    return dateRange;
  }

  public static class ReportMergeException extends java.lang.Exception {
    private static final long serialVersionUID = 1L;

    public ReportMergeException(String message) {
      super(message);
    }
  }

  public static class ReportSplitException extends java.lang.Exception {

    public ReportSplitException(String message) {
      super(message);
    }
  }
}
