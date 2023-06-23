package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.olf.erm.usage.counter41.csv.mapper.DozerMappingUtil.createDozerBeanMapper;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXB;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.niso.schemas.counter.DataType;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.Report.Customer;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceByActivityParser;
import org.olf.erm.usage.counter41.csv.mapper.MapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.dozer.CsvDozerBeanReader;
import org.supercsv.io.dozer.ICsvDozerBeanReader;
import org.supercsv.prefs.CsvPreference;

public abstract class AbstractCsvToReportMapper implements CsvToReportMapper {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final String csvString;

  abstract String getTitle();

  abstract String getName();

  abstract int getContentIndex();

  abstract DataType getDataType();

  List<ReportItem> processReportItems(List<ReportItem> reportItems) {
    return reportItems;
  }

  private static List<String> getFound(String contents, String regex) {
    if (isEmpty(regex) || isEmpty(contents)) {
      return Collections.emptyList();
    }
    List<String> results = new ArrayList<>();
    Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CASE);
    Matcher matcher = pattern.matcher(contents);

    while (matcher.find()) {
      if (matcher.groupCount() > 0) {
        results.add(matcher.group(1));
      } else {
        results.add(matcher.group());
      }
    }
    return results;
  }

  @Override
  public Report toReport() throws MapperException, IOException {
    StringReader stringReader = new StringReader(csvString);
    List<String> lines = IOUtils.readLines(stringReader);

    if (lines.size() < getContentIndex() + 1) {
      throw new MapperException("Invalid report supplied");
    }
    List<String> headerColumn = getHeaderColumn(lines.subList(0, 9));

    Report report = new Report();
    Customer customer = new Customer();
    report.getCustomer().add(customer);
    report.setTitle(getTitle());
    report.setName(getName());
    report.setVersion("4");

    customer.setID(headerColumn.get(1));
    customer.setName(headerColumn.get(2));

    if (!hasValidDates(headerColumn.get(4))) {
      throw new MapperException("Invalid date range");
    }

    List<YearMonth> yearMonths = getYearMonths(headerColumn.get(4));

    List<ReportItem> reportItems =
        getReportItems(lines.subList(getContentIndex(), lines.size()), yearMonths);
    customer.getReportItems().addAll(reportItems);

    // marshal and unmarshal to get rid of null entries
    StringWriter sw = new StringWriter();
    JAXB.marshal(report, sw);
    return JAXB.unmarshal(new StringReader(sw.toString()), Report.class);
  }

  public List<ReportItem> getReportItems(List<String> contentLines, List<YearMonth> yearMonths) {
    try (ICsvDozerBeanReader beanReader =
        new CsvDozerBeanReader(
            new StringReader(StringUtils.join(contentLines, System.lineSeparator())),
            CsvPreference.STANDARD_PREFERENCE,
            createDozerBeanMapper())) {
      beanReader.configureBeanMapping(
          ReportItem.class, createFieldMapping(yearMonths), createHintTypes(yearMonths));
      List<ReportItem> reportItems = new ArrayList<>();
      ReportItem reportItem;
      while ((reportItem = beanReader.read(ReportItem.class, createProcessors(yearMonths)))
          != null) {
        reportItem.setItemDataType(getDataType());
        reportItems.add(reportItem);
      }

      return processReportItems(reportItems);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  String[] createFieldMapping(List<YearMonth> yearMonths) {
    String[] mapping = new String[] {"itemPlatform", "itemPublisher", null, null};
    AtomicInteger atomicInt = new AtomicInteger(0);
    Stream<String> rest =
        yearMonths.stream()
            .map(ym -> String.format("itemPerformance[%d]", atomicInt.getAndIncrement()));
    return Stream.concat(Arrays.stream(mapping), rest).toArray(String[]::new);
  }

  Class<?>[] createHintTypes(List<YearMonth> yearMonths) {
    Class<?>[] first = new Class<?>[] {null, null, null, null};
    Stream<Class<Metric>> rest = yearMonths.stream().map(ym -> Metric.class);
    return Stream.concat(Arrays.stream(first), rest).toArray(Class<?>[]::new);
  }

  CellProcessor[] createProcessors(List<YearMonth> yearMonths) {
    List<CellProcessor> first =
        Arrays.asList(new NotNull(), new NotNull(), new NotNull(), new Optional());

    Stream<Optional> rest =
        yearMonths.stream().map(ym -> new Optional(new MonthPerformanceByActivityParser(ym, 2)));
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }

  private List<String> getHeaderColumn(List<String> header) {
    try (CsvListReader csvListReader =
        new CsvListReader(
            new StringReader(StringUtils.join(header, System.lineSeparator())),
            CsvPreference.STANDARD_PREFERENCE)) {
      List<String> headerColumn = new ArrayList<>();
      List<String> line;
      while ((line = csvListReader.read()) != null) {
        headerColumn.add(line.get(0));
      }
      return headerColumn;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private boolean hasValidDates(String dateRangeString) {
    try {
      List<LocalDate> dates =
          getFound(dateRangeString, "\\d{4}-\\d{2}-\\d{2}").stream()
              .map(LocalDate::parse)
              .collect(Collectors.toList());
      return dates.size() == 2
          && dates.get(0).getDayOfMonth() == 1
          && dates.get(1).getDayOfMonth()
              == YearMonth.from(dates.get(1)).atEndOfMonth().getDayOfMonth()
          && dates.get(0).compareTo(dates.get(1)) < 0;
    } catch (Exception e) {
      log.error("Error getting DateRange");
      return false;
    }
  }

  private List<YearMonth> getYearMonths(String dateRangeString) {
    List<String> strings = getFound(dateRangeString, "\\d{4}-\\d{2}");
    YearMonth start = YearMonth.parse(strings.get(0));
    YearMonth end = YearMonth.parse(strings.get(1));
    long diff = start.until(end, ChronoUnit.MONTHS);
    return Stream.iterate(start, next -> next.plusMonths(1))
        .limit(diff + 1)
        .collect(Collectors.toList());
  }

  public AbstractCsvToReportMapper(String csvString) {
    this.csvString = csvString;
  }
}
