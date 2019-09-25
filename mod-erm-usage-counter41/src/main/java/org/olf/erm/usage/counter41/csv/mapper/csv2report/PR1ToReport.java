package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.niso.schemas.counter.DataType;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.ReportItem;

public class PR1ToReport extends AbstractCsvToReportMapper {

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
  DataType getDataType() {
    return DataType.PLATFORM;
  }

  @Override
  List<ReportItem> processReportItems(List<ReportItem> reportItems) {
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
  }
}
