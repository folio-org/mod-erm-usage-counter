package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERTitleUsage;

public class TitleUsageMerger extends AbstractMerger<COUNTERTitleUsage> {

  @Override
  public List<COUNTERTitleUsage> mergeItems(List<COUNTERTitleUsage> items) {
    // merge by itemID
    ArrayList<COUNTERTitleUsage> result =
        new ArrayList<>(
            items.stream()
                .collect(Collectors.toMap(COUNTERTitleUsage::getItemID, tU -> tU, this::merge))
                .values());

    result.forEach(ctu -> ctu.setPerformance(removeNullPerformances(ctu.getPerformance())));

    result.forEach(ctu -> ctu.setPerformance(mergeItemPerformances(ctu.getPerformance())));

    return result;
  }

  private COUNTERTitleUsage merge(COUNTERTitleUsage a, COUNTERTitleUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
