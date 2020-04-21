package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERDatabaseUsage;

public class DatabaseUsageMerger extends AbstractMerger<COUNTERDatabaseUsage> {

  @Override
  public List<COUNTERDatabaseUsage> mergeItems(List<COUNTERDatabaseUsage> items) {
    // merge by platform
    ArrayList<COUNTERDatabaseUsage> result = new ArrayList<>(
        items.stream()
            .collect(
                Collectors
                    .toMap(COUNTERDatabaseUsage::getDatabase, it -> it, this::merge))
            .values());

    result.forEach(ctu ->
        ctu.setPerformance(removeNullPerformances(ctu.getPerformance()))
    );

    result
        .forEach(ctu ->
            ctu.setPerformance(mergeItemPerformances(ctu.getPerformance()))
        );
    return result;
  }

  private COUNTERDatabaseUsage merge(COUNTERDatabaseUsage a, COUNTERDatabaseUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
