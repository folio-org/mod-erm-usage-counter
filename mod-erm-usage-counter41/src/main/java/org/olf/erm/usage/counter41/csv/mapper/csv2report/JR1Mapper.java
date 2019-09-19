package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import java.io.IOException;
import java.io.StringReader;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.niso.schemas.counter.Category;
import org.niso.schemas.counter.DataType;
import org.niso.schemas.counter.Identifier;
import org.niso.schemas.counter.IdentifierType;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.csv.cellprocessor.IdentifierParser;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanReader;
import org.supercsv.io.dozer.ICsvDozerBeanReader;
import org.supercsv.prefs.CsvPreference;

public class JR1Mapper extends AbstractCsvToReportMapper {

  private static final Logger log = LoggerFactory.getLogger(JR1Mapper.class);

  public JR1Mapper(String csvString) {
    super(csvString);
  }

  private String[] createFieldMapping(List<YearMonth> yearMonths) {
    String[] mapping =
        new String[] {
          "itemName",
          "itemPublisher",
          "itemPlatform",
          "itemIdentifier[0]",
          "itemIdentifier[1]",
          "itemIdentifier[2]",
          "itemIdentifier[3]",
          null,
          null,
          null
        };
    AtomicInteger atomicInt = new AtomicInteger(0);
    Stream<String> rest =
        yearMonths.stream()
            .map(ym -> String.format("itemPerformance[%d]", atomicInt.getAndIncrement()));
    return Stream.concat(Arrays.stream(mapping), rest).toArray(String[]::new);
  }

  private Class<?>[] createHintTypes(List<YearMonth> yearMonths) {
    Class<?>[] first =
        new Class<?>[] {
          null,
          null,
          null,
          Identifier.class,
          Identifier.class,
          Identifier.class,
          Identifier.class,
          null,
          null,
          null
        };
    Stream<Class<Metric>> rest = yearMonths.stream().map(ym -> Metric.class);
    return Stream.concat(Arrays.stream(first), rest).toArray(Class<?>[]::new);
  }

  private CellProcessor[] createProcessors(List<YearMonth> yearMonths) {
    List<CellProcessor> first =
        Arrays.asList(
            new NotNull(),
            new NotNull(),
            new NotNull(),
            new Optional(new IdentifierParser(IdentifierType.DOI)),
            new Optional(new IdentifierParser(IdentifierType.PROPRIETARY)),
            new Optional(new IdentifierParser(IdentifierType.PRINT_ISSN)),
            new Optional(new IdentifierParser(IdentifierType.ONLINE_ISSN)),
            new Optional(),
            new Optional(),
            new Optional());
    Stream<Optional> rest =
        yearMonths.stream()
            .map(
                ym ->
                    new Optional(
                        new MonthPerformanceParser(ym, MetricType.FT_TOTAL, Category.REQUESTS)));
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }

  public List<ReportItem> getReportItems(List<String> contentLines, List<YearMonth> yearMonths) {
    try (ICsvDozerBeanReader beanReader =
        new CsvDozerBeanReader(
            new StringReader(StringUtils.join(contentLines, System.lineSeparator())),
            CsvPreference.STANDARD_PREFERENCE)) {
      beanReader.configureBeanMapping(
          ReportItem.class, createFieldMapping(yearMonths), createHintTypes(yearMonths));
      List<ReportItem> reportItems = new ArrayList<>();
      ReportItem reportItem;
      while ((reportItem = beanReader.read(ReportItem.class, createProcessors(yearMonths)))
          != null) {
        reportItem.setItemDataType(DataType.JOURNAL);
        reportItems.add(reportItem);
      }
      return reportItems;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }
}
