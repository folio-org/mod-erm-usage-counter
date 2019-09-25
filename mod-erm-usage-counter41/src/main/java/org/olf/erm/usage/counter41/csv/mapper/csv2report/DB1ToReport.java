package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.niso.schemas.counter.DataType;
import org.niso.schemas.counter.Metric;
import org.olf.erm.usage.counter41.csv.cellprocessor.MonthPerformanceByActivityParser;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class DB1ToReport extends PR1ToReport {

  public DB1ToReport(String csvString) {
    super(csvString);
  }

  @Override
  String getTitle() {
    return "Database Report 1";
  }

  @Override
  String getName() {
    return "DB1";
  }

  @Override
  DataType getDataType() {
    return DataType.DATABASE;
  }

  @Override
  String[] createFieldMapping(List<YearMonth> yearMonths) {
    String[] mapping = new String[] {"itemName", "itemPublisher", "itemPlatform", null, null};
    AtomicInteger atomicInt = new AtomicInteger(0);
    Stream<String> rest =
        yearMonths.stream()
            .map(ym -> String.format("itemPerformance[%d]", atomicInt.getAndIncrement()));
    return Stream.concat(Arrays.stream(mapping), rest).toArray(String[]::new);
  }

  @Override
  Class<?>[] createHintTypes(List<YearMonth> yearMonths) {
    Class<?>[] first = new Class<?>[] {null, null, null, null, null};
    Stream<Class<Metric>> rest = yearMonths.stream().map(ym -> Metric.class);
    return Stream.concat(Arrays.stream(first), rest).toArray(Class<?>[]::new);
  }

  @Override
  CellProcessor[] createProcessors(List<YearMonth> yearMonths) {
    List<CellProcessor> first =
        Arrays.asList(new NotNull(), new NotNull(), new NotNull(), new NotNull(), new Optional());

    Stream<Optional> rest =
        yearMonths.stream().map(ym -> new Optional(new MonthPerformanceByActivityParser(ym, 3)));
    return Stream.concat(first.stream(), rest).toArray(CellProcessor[]::new);
  }
}
