package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.niso.schemas.counter.Category;
import org.niso.schemas.counter.DataType;
import org.niso.schemas.counter.Identifier;
import org.niso.schemas.counter.IdentifierType;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.MetricType;
import org.olf.erm.usage.counter41.csv.cellprocessor.IdentifierParser;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceParser;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class BR1ToReport extends AbstractCsvToReportMapper {

  public BR1ToReport(String csvString) {
    super(csvString);
  }

  @Override
  String getTitle() {
    return "Book Report 1";
  }

  @Override
  String getName() {
    return "BR1";
  }

  @Override
  int getContentIndex() {
    return 9;
  }

  @Override
  DataType getDataType() {
    return DataType.BOOK;
  }

  @Override
  String[] createFieldMapping(List<YearMonth> yearMonths) {
    String[] mapping =
        new String[] {
          "itemName",
          "itemPublisher",
          "itemPlatform",
          "itemIdentifier[0]",
          "itemIdentifier[1]",
          "itemIdentifier[2]",
          "itemIdentifier[3]",
          null
        };
    AtomicInteger atomicInt = new AtomicInteger(0);
    Stream<String> rest =
        yearMonths.stream()
            .map(ym -> String.format("itemPerformance[%d]", atomicInt.getAndIncrement()));
    return Stream.concat(Arrays.stream(mapping), rest).toArray(String[]::new);
  }

  @Override
  Class<?>[] createHintTypes(List<YearMonth> yearMonths) {
    Class<?>[] first =
        new Class<?>[] {
          null,
          null,
          null,
          Identifier.class,
          Identifier.class,
          Identifier.class,
          Identifier.class,
          null
        };
    Stream<Class<Metric>> rest = yearMonths.stream().map(ym -> Metric.class);
    return Stream.concat(Arrays.stream(first), rest).toArray(Class<?>[]::new);
  }

  @Override
  CellProcessor[] createProcessors(List<YearMonth> yearMonths) {
    List<CellProcessor> first =
        Arrays.asList(
            new NotNull(),
            new Optional(),
            new Optional(),
            new IdentifierParser(IdentifierType.DOI),
            new Optional(new IdentifierParser(IdentifierType.PROPRIETARY)),
            new Optional(new IdentifierParser(IdentifierType.PRINT_ISBN)),
            new Optional(new IdentifierParser(IdentifierType.PRINT_ISSN)),
            new Optional());
    Stream<Optional> rest =
        yearMonths.stream()
            .map(
                ym ->
                    new Optional(
                        new MonthPerformanceParser(ym, MetricType.FT_TOTAL, Category.REQUESTS)));
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }
}
