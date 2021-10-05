package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERPlatformUsage;

public class PlatformUsageMerger extends AbstractMerger<COUNTERPlatformUsage> {

  @Override
  public List<COUNTERPlatformUsage> mergeItems(List<COUNTERPlatformUsage> items) {
    // merge by everything except performance
    ArrayList<COUNTERPlatformUsage> result =
        new ArrayList<>(
            items.stream()
                .collect(
                    Collectors.toMap(
                        cpu ->
                            Arrays.asList(
                                cpu.getPlatform(), cpu.getDataType(), cpu.getAccessMethod()),
                        cpu -> cpu,
                        this::merge))
                .values());

    result.forEach(cpu -> cpu.setPerformance(removeNullPerformances(cpu.getPerformance())));

    result.forEach(cpu -> cpu.setPerformance(mergeItemPerformances(cpu.getPerformance())));
    return result;
  }

  private COUNTERPlatformUsage merge(COUNTERPlatformUsage a, COUNTERPlatformUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
