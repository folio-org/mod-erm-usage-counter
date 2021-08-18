package org.olf.erm.usage.counter50.converter.tr;

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
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;

public abstract class TRConverterBase implements Converter<COUNTERTitleReport> {

  protected Function<COUNTERTitleUsage, List<Object>> getGroupByAttributes() {
    return u ->
        Arrays.asList(
            u.getTitle(), u.getItemID(), u.getPlatform(), u.getPublisher(), u.getPublisherID());
  }

  protected List<COUNTERTitleUsage> createNewReportItems(
      List<COUNTERTitleUsage> reportItems,
      Predicate<COUNTERTitleUsage> filter,
      UnaryOperator<COUNTERTitleUsage> mapper) {

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
