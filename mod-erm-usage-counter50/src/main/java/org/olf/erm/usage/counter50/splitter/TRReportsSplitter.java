package org.olf.erm.usage.counter50.splitter;

import java.util.List;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERTitleReport;
import org.openapitools.counter50.model.COUNTERTitleUsage;
import org.openapitools.counter50.model.SUSHIReportHeader;

public class TRReportsSplitter
    extends AbstractReportsSplitter<COUNTERTitleReport, COUNTERTitleUsage> {

  @Override
  protected SUSHIReportHeader getReportHeader(COUNTERTitleReport report) {
    return report.getReportHeader();
  }

  @Override
  protected List<COUNTERTitleUsage> getReportItems(COUNTERTitleReport report) {
    return report.getReportItems();
  }

  @Override
  protected List<COUNTERItemPerformance> getPerformance(COUNTERTitleUsage reportItem) {
    return reportItem.getPerformance();
  }

  @Override
  protected COUNTERTitleUsage createReportItemForPeriod(
      COUNTERTitleUsage reportItem, List<COUNTERItemPerformance> performanceOfPeriod) {
    return new COUNTERTitleUsage()
        .title(reportItem.getTitle())
        .itemID(modifiableCopyOf(reportItem.getItemID()))
        .platform(reportItem.getPlatform())
        .publisher(reportItem.getPublisher())
        .publisherID(modifiableCopyOf(reportItem.getPublisherID()))
        .dataType(reportItem.getDataType())
        .sectionType(reportItem.getSectionType())
        .YOP(reportItem.getYOP())
        .accessType(reportItem.getAccessType())
        .accessMethod(reportItem.getAccessMethod())
        .performance(performanceOfPeriod);
  }

  @Override
  protected COUNTERTitleReport createReport(
      SUSHIReportHeader reportHeader, List<COUNTERTitleUsage> reportItems) {
    return new COUNTERTitleReport().reportHeader(reportHeader).reportItems(reportItems);
  }

  /**
   * Declared so that code compiled against earlier versions keeps linking: the method inherited
   * from {@link AbstractReportsSplitter} erases to {@code split(Object)} and would not provide
   * {@code split(COUNTERTitleReport)}.
   */
  @SuppressWarnings("java:S1185") // not redundant: see javadoc above
  @Override
  public List<COUNTERTitleReport> split(COUNTERTitleReport report) {
    return super.split(report);
  }
}
