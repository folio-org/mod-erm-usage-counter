package org.olf.erm.usage.counter50.merger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERPlatformUsage;
import org.openapitools.client.model.SUSHIReportHeader;

public class PRReportsMerger extends ReportsMerger<COUNTERPlatformReport> {

  @Override
  public COUNTERPlatformReport merge(List<COUNTERPlatformReport> reports) {
    List<SUSHIReportHeader> headers =
        reports.stream().map(COUNTERPlatformReport::getReportHeader).collect(Collectors.toList());
    SUSHIReportHeader mergedHeader = mergeHeaders(headers);
    List<COUNTERPlatformUsage> mergedReportItems =
        reports.stream().flatMap(r -> r.getReportItems().stream()).collect(Collectors.toList());

    mergedReportItems = mergePlatformUsages(mergedReportItems);

    COUNTERPlatformReport result = new COUNTERPlatformReport();
    result.setReportHeader(mergedHeader);
    result.setReportItems(mergedReportItems);
    return result;
  }

  private List<COUNTERPlatformUsage> mergePlatformUsages(
      List<COUNTERPlatformUsage> platformUsages) {
    return new ArrayList<>(
        platformUsages.stream()
            .collect(
                Collectors.toMap(
                    cpu ->
                        Arrays.asList(cpu.getPlatform(), cpu.getDataType(), cpu.getAccessMethod()),
                    cpu -> cpu,
                    this::merge))
            .values());
  }

  private COUNTERPlatformUsage merge(COUNTERPlatformUsage a, COUNTERPlatformUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
