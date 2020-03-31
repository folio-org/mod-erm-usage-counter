package org.olf.erm.usage.counter50.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERItemUsage;
import org.openapitools.client.model.SUSHIReportHeader;

public class IRReportsMerger extends ReportsMerger<COUNTERItemReport> {

  @Override
  public COUNTERItemReport merge(List<COUNTERItemReport> reports) {
    List<SUSHIReportHeader> headers = reports.stream().map(COUNTERItemReport::getReportHeader)
        .collect(Collectors.toList());
    SUSHIReportHeader mergedHeader = mergeHeaders(headers);
    List<COUNTERItemUsage> mergedReportItems = reports.stream()
        .flatMap(r -> r.getReportItems().stream()).collect(Collectors.toList());
    mergedReportItems = mergeReportItemsWithSameID(mergedReportItems);

    COUNTERItemReport result = new COUNTERItemReport();
    result.setReportHeader(mergedHeader);
    result.setReportItems(mergedReportItems);
    return result;
  }

  private List<COUNTERItemUsage> mergeReportItemsWithSameID(List<COUNTERItemUsage> reportItems) {

    Map<String, COUNTERItemUsage> collect = reportItems.stream().collect(Collectors
        .groupingBy(this::getDOI,
            Collectors
                .collectingAndThen(Collectors.reducing(this::merge), Optional::get)));
    return new ArrayList<>(collect.values());

  }

  private String getDOI(COUNTERItemUsage itemUsage) {
    Optional<String> doi = itemUsage.getItemID().stream()
        .filter(itemID -> itemID.getType() == TypeEnum.DOI).findFirst()
        .map(COUNTERItemIdentifiers::getValue);
    if (doi.isPresent()) {
      return doi.get();
    } else {
      throw new RuntimeException(
          String.format("DOI not present in COUNTERItemUsage of %s", itemUsage.getItem()));
    }
  }

  private COUNTERItemUsage merge(COUNTERItemUsage a, COUNTERItemUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
