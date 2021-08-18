package org.olf.erm.usage.counter50.converter.dr;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.olf.erm.usage.counter50.converter.Converter;
import org.olf.erm.usage.counter50.converter.ConverterUtils;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERDatabaseUsage;

public abstract class DRConverterBase implements Converter<COUNTERDatabaseReport> {

  protected Function<COUNTERDatabaseUsage, List<Object>> getGroupByAttributes() {
    return u ->
        Arrays.asList(
            u.getDatabase(), u.getPlatform(), u.getItemID(), u.getPublisher(), u.getPublisherID());
  }

  protected List<COUNTERDatabaseUsage> createNewReportItems(
      List<COUNTERDatabaseUsage> reportItems,
      Predicate<COUNTERDatabaseUsage> filter,
      UnaryOperator<COUNTERDatabaseUsage> mapper) {

    return reportItems.stream()
        .filter(filter)
        .collect( // sum grouped item performances
            groupingBy(
                getGroupByAttributes(),
                reducing(
                    (u1, u2) ->
                        u1.performance(
                            ConverterUtils.sumCOUNTERItemPerformance(
                                u1.getPerformance(), u2.getPerformance())))))
        .values()
        .stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(mapper)
        .collect(toList());
  }
}
