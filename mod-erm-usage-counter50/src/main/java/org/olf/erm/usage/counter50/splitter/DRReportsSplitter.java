package org.olf.erm.usage.counter50.splitter;

import java.util.List;
import org.openapitools.counter50.model.COUNTERDatabaseReport;
import org.openapitools.counter50.model.COUNTERDatabaseUsage;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.SUSHIReportHeader;

public class DRReportsSplitter
    extends AbstractReportsSplitter<COUNTERDatabaseReport, COUNTERDatabaseUsage> {

  @Override
  protected SUSHIReportHeader getReportHeader(COUNTERDatabaseReport report) {
    return report.getReportHeader();
  }

  @Override
  protected List<COUNTERDatabaseUsage> getReportItems(COUNTERDatabaseReport report) {
    return report.getReportItems();
  }

  @Override
  protected List<COUNTERItemPerformance> getPerformance(COUNTERDatabaseUsage reportItem) {
    return reportItem.getPerformance();
  }

  @Override
  protected COUNTERDatabaseUsage createReportItemForPeriod(
      COUNTERDatabaseUsage reportItem, List<COUNTERItemPerformance> performanceOfPeriod) {
    return new COUNTERDatabaseUsage()
        .database(reportItem.getDatabase())
        .itemID(modifiableCopyOf(reportItem.getItemID()))
        .platform(reportItem.getPlatform())
        .publisher(reportItem.getPublisher())
        .publisherID(modifiableCopyOf(reportItem.getPublisherID()))
        .dataType(reportItem.getDataType())
        .accessMethod(reportItem.getAccessMethod())
        .performance(performanceOfPeriod);
  }

  @Override
  protected COUNTERDatabaseReport createReport(
      SUSHIReportHeader reportHeader, List<COUNTERDatabaseUsage> reportItems) {
    return new COUNTERDatabaseReport().reportHeader(reportHeader).reportItems(reportItems);
  }

  /**
   * Declared so that code compiled against earlier versions keeps linking: the method inherited
   * from {@link AbstractReportsSplitter} erases to {@code split(Object)} and would not provide
   * {@code split(COUNTERDatabaseReport)}.
   */
  @SuppressWarnings("java:S1185") // not redundant: see javadoc above
  @Override
  public List<COUNTERDatabaseReport> split(COUNTERDatabaseReport report) {
    return super.split(report);
  }
}
