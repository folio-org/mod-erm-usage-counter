package org.olf.erm.usage.counter50.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;
import org.openapitools.client.model.SUSHIReportHeader;

public class TRReportsMerger extends ReportsMerger<COUNTERTitleReport> {

  @Override
  public COUNTERTitleReport merge(List<COUNTERTitleReport> reports) {
    List<SUSHIReportHeader> headers = reports.stream().map(COUNTERTitleReport::getReportHeader)
        .collect(Collectors.toList());
    SUSHIReportHeader mergedHeader = mergeHeaders(headers);
    List<COUNTERTitleUsage> mergedReportItems = reports.stream()
        .flatMap(r -> r.getReportItems().stream()).collect(Collectors.toList());
    mergedReportItems = mergeTitleUsages(mergedReportItems);

    COUNTERTitleReport result = new COUNTERTitleReport();
    result.setReportHeader(mergedHeader);
    result.setReportItems(mergedReportItems);
    return result;
  }

  private List<COUNTERTitleUsage> mergeTitleUsages(List<COUNTERTitleUsage> titleUsages) {
    Map<String, COUNTERTitleUsage> collect = titleUsages.stream().collect(Collectors
        .groupingBy(this::getDOI,
            Collectors
                .collectingAndThen(Collectors.reducing(this::merge), Optional::get)));
    return new ArrayList<>(collect.values());
  }


  private String getDOI(COUNTERTitleUsage titleUsage) {
    Optional<String> doi = titleUsage.getItemID().stream()
        .filter(itemID -> itemID.getType() == TypeEnum.DOI).findFirst()
        .map(COUNTERItemIdentifiers::getValue);
    if (doi.isPresent()) {
      return doi.get();
    } else {
      throw new RuntimeException(
          String.format("DOI not present in COUNTETileUsage of %s", titleUsage.getTitle()));
    }
  }

  private COUNTERTitleUsage merge(COUNTERTitleUsage a, COUNTERTitleUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }


}
