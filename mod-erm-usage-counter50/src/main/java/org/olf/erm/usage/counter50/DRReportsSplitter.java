package org.olf.erm.usage.counter50;

import java.util.List;
import org.openapitools.counter50.model.COUNTERDatabaseReport;
import org.openapitools.counter50.model.COUNTERDatabaseUsage;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.SUSHIReportHeader;

class DRReportsSplitter
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
}
