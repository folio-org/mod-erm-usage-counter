package org.olf.erm.usage.counter50.merger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.counter50.model.COUNTERItemReport;
import org.openapitools.counter50.model.COUNTERItemUsage;
import org.openapitools.counter50.model.SUSHIReportHeader;

public class IRReportsMerger extends ReportsMerger<COUNTERItemReport> {

  @Override
  public COUNTERItemReport merge(List<COUNTERItemReport> reports) {
    List<SUSHIReportHeader> headers =
        reports.stream().map(COUNTERItemReport::getReportHeader).collect(Collectors.toList());
    SUSHIReportHeader mergedHeader = mergeHeaders(headers);
    List<COUNTERItemUsage> mergedReportItems =
        reports.stream().flatMap(r -> r.getReportItems().stream()).collect(Collectors.toList());
    mergedReportItems = mergeReportItemsWithSameID(mergedReportItems);

    COUNTERItemReport result = new COUNTERItemReport();
    result.setReportHeader(mergedHeader);
    result.setReportItems(mergedReportItems);
    return result;
  }

  public List<COUNTERItemUsage> mergeReportItemsWithSameID(List<COUNTERItemUsage> reportItems) {
    return new ArrayList<>(
        reportItems.stream()
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
  }

  private COUNTERItemUsage merge(COUNTERItemUsage a, COUNTERItemUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
