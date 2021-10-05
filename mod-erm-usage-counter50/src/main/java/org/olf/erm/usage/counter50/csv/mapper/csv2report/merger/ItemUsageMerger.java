package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERItemUsage;

public class ItemUsageMerger extends AbstractMerger<COUNTERItemUsage> {

  @Override
  public List<COUNTERItemUsage> mergeItems(List<COUNTERItemUsage> items) {
    // merge by everything exept performance
    ArrayList<COUNTERItemUsage> result =
        new ArrayList<>(
            items.stream()
                .collect(
                    Collectors.toMap(
                        ciu ->
                            Arrays.asList(
                                ciu.getItem(),
                                ciu.getPublisher(),
                                ciu.getPublisherID(),
                                ciu.getItemID(),
                                ciu.getDataType(),
                                ciu.getAccessMethod(),
                                ciu.getAccessType(),
                                ciu.getPlatform(),
                                ciu.getYOP(),
                                ciu.getItemAttributes(),
                                ciu.getItemComponent(),
                                ciu.getItemContributors(),
                                ciu.getItemDates(),
                                ciu.getItemParent()),
                        ciu -> ciu,
                        this::merge))
                .values());

    result.forEach(ciu -> ciu.setPerformance(removeNullPerformances(ciu.getPerformance())));

    result.forEach(ciu -> ciu.setPerformance(mergeItemPerformances(ciu.getPerformance())));
    return result;
  }

  private COUNTERItemUsage merge(COUNTERItemUsage a, COUNTERItemUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
