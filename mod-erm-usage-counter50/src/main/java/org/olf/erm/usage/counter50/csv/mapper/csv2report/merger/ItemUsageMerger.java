package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERItemUsage;

public class ItemUsageMerger extends AbstractMerger<COUNTERItemUsage> {

  @Override
  public List<COUNTERItemUsage> mergeItems(List<COUNTERItemUsage> items) {
    // merge by itemID
    ArrayList<COUNTERItemUsage> result = new ArrayList<>(
        items.stream()
            .collect(
                Collectors
                    .toMap(COUNTERItemUsage::getItemID, it -> it, this::merge))
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

  private COUNTERItemUsage merge(COUNTERItemUsage a, COUNTERItemUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
