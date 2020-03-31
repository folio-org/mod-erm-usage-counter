package org.olf.erm.usage.counter50.merger;

import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERPlatformUsage;
import org.openapitools.client.model.SUSHIReportHeader;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PRReportsMerger extends ReportsMerger<COUNTERPlatformReport> {

  @Override
  public COUNTERPlatformReport merge(List<COUNTERPlatformReport> reports) {
    List<SUSHIReportHeader> headers = reports.stream().map(
        COUNTERPlatformReport::getReportHeader).collect(Collectors.toList());
    SUSHIReportHeader mergedHeader = mergeHeaders(headers);

    /*
     * As we are merging platform reports for only one platform, we extract first COUNTERPlatformUsage and then set its performance to all COUNTERItemPerformances of given reports
     */
    List<COUNTERItemPerformance> itemPerformances = reports.stream()
        .flatMap(r -> r.getReportItems().stream().flatMap(item -> item.getPerformance().stream()))
        .collect(Collectors.toList());
    Optional<COUNTERPlatformUsage> first = reports.stream().map(
        counterPlatformReport -> counterPlatformReport.getReportItems().stream().findFirst()
            .orElse(null))
        .findFirst();
    COUNTERPlatformUsage platformUsage = first.orElse(new COUNTERPlatformUsage());
    platformUsage.setPerformance(itemPerformances);

    COUNTERPlatformReport result = new COUNTERPlatformReport();
    result.setReportHeader(mergedHeader);
    result.setReportItems(Arrays.asList(platformUsage));
    return result;
  }

}
