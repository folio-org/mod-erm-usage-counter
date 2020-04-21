package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERPlatformUsage;

public class PlatformUsageMerger extends AbstractMerger<COUNTERPlatformUsage> {

  @Override
  public List<COUNTERPlatformUsage> mergeItems(List<COUNTERPlatformUsage> items) {
    // merge by platform
    ArrayList<COUNTERPlatformUsage> result = new ArrayList<>(
        items.stream()
            .collect(
                Collectors
                    .toMap(COUNTERPlatformUsage::getPlatform, it -> it, this::merge))
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

  private COUNTERPlatformUsage merge(COUNTERPlatformUsage a, COUNTERPlatformUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
