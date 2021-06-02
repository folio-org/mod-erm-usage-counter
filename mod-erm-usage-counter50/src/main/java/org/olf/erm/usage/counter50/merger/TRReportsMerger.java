package org.olf.erm.usage.counter50.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;
import org.openapitools.client.model.SUSHIReportHeader;

public class TRReportsMerger extends ReportsMerger<COUNTERTitleReport> {

  @Override
  public COUNTERTitleReport merge(List<COUNTERTitleReport> reports) {
    List<SUSHIReportHeader> headers =
        reports.stream().map(COUNTERTitleReport::getReportHeader).collect(Collectors.toList());
    SUSHIReportHeader mergedHeader = mergeHeaders(headers);
    List<COUNTERTitleUsage> mergedReportItems =
        reports.stream().flatMap(r -> r.getReportItems().stream()).collect(Collectors.toList());
    mergedReportItems = mergeTitleUsages(mergedReportItems);

    COUNTERTitleReport result = new COUNTERTitleReport();
    result.setReportHeader(mergedHeader);
    result.setReportItems(mergedReportItems);
    return result;
  }

  private List<COUNTERTitleUsage> mergeTitleUsages(List<COUNTERTitleUsage> titleUsages) {
    return new ArrayList<>(
        titleUsages.stream()
            .collect(
                Collectors.toMap(
                    COUNTERTitleUsage::getItemID, titleUsage -> titleUsage, this::merge))
            .values());
  }

  private COUNTERTitleUsage merge(COUNTERTitleUsage a, COUNTERTitleUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
