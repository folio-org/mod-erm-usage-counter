package org.olf.erm.usage.counter50.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERDatabaseUsage;
import org.openapitools.client.model.SUSHIReportHeader;

public class DRReportsMerger extends ReportsMerger<COUNTERDatabaseReport> {

  @Override
  public COUNTERDatabaseReport merge(List<COUNTERDatabaseReport> reports) {
    List<SUSHIReportHeader> headers =
        reports.stream().map(COUNTERDatabaseReport::getReportHeader).collect(Collectors.toList());
    SUSHIReportHeader mergedHeader = mergeHeaders(headers);

    List<COUNTERDatabaseUsage> mergedReportItems =
        reports.stream().flatMap(r -> r.getReportItems().stream()).collect(Collectors.toList());
    mergedReportItems = mergeDatabaseUsages(mergedReportItems);

    COUNTERDatabaseReport result = new COUNTERDatabaseReport();
    result.setReportHeader(mergedHeader);
    result.setReportItems(mergedReportItems);
    return result;
  }

  private List<COUNTERDatabaseUsage> mergeDatabaseUsages(List<COUNTERDatabaseUsage> dbUsages) {
    return new ArrayList<>(
        dbUsages.stream()
            .collect(Collectors.toMap(COUNTERDatabaseUsage::getDatabase, t -> t, this::merge))
            .values());
  }

  private COUNTERDatabaseUsage merge(COUNTERDatabaseUsage a, COUNTERDatabaseUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
