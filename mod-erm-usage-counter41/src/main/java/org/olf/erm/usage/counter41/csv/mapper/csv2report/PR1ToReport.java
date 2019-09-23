package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import java.io.IOException;
import java.io.StringReader;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.niso.schemas.counter.DataType;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceByActivityParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanReader;
import org.supercsv.io.dozer.ICsvDozerBeanReader;
import org.supercsv.prefs.CsvPreference;

public class PR1ToReport extends AbstractCsvToReportMapper {

  private static final Logger log = LoggerFactory.getLogger(PR1ToReport.class);

  public PR1ToReport(String csvString) {
    super(csvString);
  }

  @Override
  String getTitle() {
    return "Platform Report 1";
  }

  @Override
  String getName() {
    return "PR1";
  }

  @Override
  int getContentIndex() {
    return 8;
  }

  @Override
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
        reportItem.setItemDataType(DataType.PLATFORM);
        reportItems.add(reportItem);
      }

      BinaryOperator<ReportItem> reportItemCombiner =
          (ri1, ri2) -> {
            ri2.getItemPerformance()
                .forEach(
                    m -> {
                      java.util.Optional<Metric> optionalMetric =
                          ri1.getItemPerformance().stream()
                              .filter(m2 -> m2.getPeriod().equals(m.getPeriod()))
                              .filter(m2 -> m2.getCategory().equals(m.getCategory()))
                              .findFirst();
                      if (optionalMetric.isPresent()) {
                        optionalMetric.get().getInstance().addAll(m.getInstance());
                      } else {
                        ri1.getItemPerformance().add(m);
                      }
                    });
            return ri1;
          };

      return reportItems.stream()
          .collect(
              Collectors.groupingBy(
                  ri -> ri.getItemPlatform() + ri.getItemPublisher(),
                  Collectors.reducing(reportItemCombiner)))
          .values()
          .stream()
          .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private String[] createFieldMapping(List<YearMonth> yearMonths) {
    String[] mapping = new String[] {"itemPlatform", "itemPublisher", null, null};
    AtomicInteger atomicInt = new AtomicInteger(0);
    Stream<String> rest =
        yearMonths.stream()
            .map(ym -> String.format("itemPerformance[%d]", atomicInt.getAndIncrement()));
    return Stream.concat(Arrays.stream(mapping), rest).toArray(String[]::new);
  }

  private Class<?>[] createHintTypes(List<YearMonth> yearMonths) {
    Class<?>[] first = new Class<?>[] {null, null, null, null};
    Stream<Class<Metric>> rest = yearMonths.stream().map(ym -> Metric.class);
    return Stream.concat(Arrays.stream(first), rest).toArray(Class<?>[]::new);
  }

  private CellProcessor[] createProcessors(List<YearMonth> yearMonths) {
    List<CellProcessor> first =
        Arrays.asList(new NotNull(), new NotNull(), new NotNull(), new Optional());

    Stream<Optional> rest =
        yearMonths.stream().map(ym -> new Optional(new MonthPerformanceByActivityParser(ym, 2)));
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }
}
