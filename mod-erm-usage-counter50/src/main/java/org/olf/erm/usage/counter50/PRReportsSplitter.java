package org.olf.erm.usage.counter50;

import java.util.List;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERPlatformReport;
import org.openapitools.counter50.model.COUNTERPlatformUsage;
import org.openapitools.counter50.model.SUSHIReportHeader;

class PRReportsSplitter
    extends AbstractReportsSplitter<COUNTERPlatformReport, COUNTERPlatformUsage> {

  @Override
  protected SUSHIReportHeader getReportHeader(COUNTERPlatformReport report) {
    return report.getReportHeader();
  }

  @Override
  protected List<COUNTERPlatformUsage> getReportItems(COUNTERPlatformReport report) {
    return report.getReportItems();
  }

  @Override
  protected List<COUNTERItemPerformance> getPerformance(COUNTERPlatformUsage reportItem) {
    return reportItem.getPerformance();
  }

  @Override
  protected COUNTERPlatformUsage createReportItemForPeriod(
      COUNTERPlatformUsage reportItem, List<COUNTERItemPerformance> performanceOfPeriod) {
    return new COUNTERPlatformUsage()
        .platform(reportItem.getPlatform())
        .dataType(reportItem.getDataType())
        .accessMethod(reportItem.getAccessMethod())
        .performance(performanceOfPeriod);
  }

  @Override
  protected COUNTERPlatformReport createReport(
      SUSHIReportHeader reportHeader, List<COUNTERPlatformUsage> reportItems) {
    return new COUNTERPlatformReport().reportHeader(reportHeader).reportItems(reportItems);
  }
}
