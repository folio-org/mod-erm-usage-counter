package org.olf.erm.usage.counter50.splitter;

import java.util.List;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERPlatformReport;
import org.openapitools.counter50.model.COUNTERPlatformUsage;
import org.openapitools.counter50.model.SUSHIReportHeader;

public class PRReportsSplitter
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

  /**
   * Declared so that code compiled against earlier versions keeps linking: the method inherited
   * from {@link AbstractReportsSplitter} erases to {@code split(Object)} and would not provide
   * {@code split(COUNTERPlatformReport)}.
   */
  @SuppressWarnings("java:S1185") // not redundant: see javadoc above
  @Override
  public List<COUNTERPlatformReport> split(COUNTERPlatformReport report) {
    return super.split(report);
  }
}
