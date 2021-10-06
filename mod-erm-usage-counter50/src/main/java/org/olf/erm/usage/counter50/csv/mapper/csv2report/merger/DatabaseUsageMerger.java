package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERDatabaseUsage;

public class DatabaseUsageMerger extends AbstractMerger<COUNTERDatabaseUsage> {

  @Override
  public List<COUNTERDatabaseUsage> mergeItems(List<COUNTERDatabaseUsage> items) {
    // merge everything except performance
    ArrayList<COUNTERDatabaseUsage> result =
        new ArrayList<>(
            items.stream()
                .collect(
                    Collectors.toMap(
                        du ->
                            Arrays.asList(
                                du.getDatabase(),
                                du.getPublisher(),
                                du.getPublisherID(),
                                du.getPlatform(),
                                du.getItemID(),
                                du.getDataType(),
                                du.getAccessMethod()),
                        du -> du,
                        this::merge))
                .values());

    result.forEach(cdu -> cdu.setPerformance(removeNullPerformances(cdu.getPerformance())));

    result.forEach(cdu -> cdu.setPerformance(mergeItemPerformances(cdu.getPerformance())));
    return result;
  }

  private COUNTERDatabaseUsage merge(COUNTERDatabaseUsage a, COUNTERDatabaseUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
