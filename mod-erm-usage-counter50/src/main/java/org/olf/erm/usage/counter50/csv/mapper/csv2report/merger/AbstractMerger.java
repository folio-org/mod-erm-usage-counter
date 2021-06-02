package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;

public abstract class AbstractMerger<T> implements Merger<T> {

  protected List<COUNTERItemPerformance> mergeItemPerformances(
      List<COUNTERItemPerformance> performances) {
    return new ArrayList<>(
        performances.stream()
            .collect(
                Collectors.toMap(
                    COUNTERItemPerformance::getPeriod,
                    counterItemPerformance -> counterItemPerformance,
                    this::mergeInnerItemPerformances))
            .values());
  }

  private COUNTERItemPerformance mergeInnerItemPerformances(
      COUNTERItemPerformance a, COUNTERItemPerformance b) {
    b.getInstance()
        .forEach(
            i -> {
              if (i.getCount() != null) {
                List<COUNTERItemPerformanceInstance> list = new ArrayList<>(a.getInstance());
                list.add(i);
                a.setInstance(list);
              }
            });
    return a;
  }

  protected List<COUNTERItemPerformance> removeNullPerformances(
      List<COUNTERItemPerformance> performances) {
    return performances.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }
}
