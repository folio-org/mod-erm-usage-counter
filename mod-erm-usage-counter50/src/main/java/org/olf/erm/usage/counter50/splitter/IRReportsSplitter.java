package org.olf.erm.usage.counter50.splitter;

import java.util.List;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERItemReport;
import org.openapitools.counter50.model.COUNTERItemUsage;
import org.openapitools.counter50.model.SUSHIReportHeader;

public class IRReportsSplitter
    extends AbstractReportsSplitter<COUNTERItemReport, COUNTERItemUsage> {

  @Override
  protected SUSHIReportHeader getReportHeader(COUNTERItemReport report) {
    return report.getReportHeader();
  }

  @Override
  protected List<COUNTERItemUsage> getReportItems(COUNTERItemReport report) {
    return report.getReportItems();
  }

  @Override
  protected List<COUNTERItemPerformance> getPerformance(COUNTERItemUsage reportItem) {
    return reportItem.getPerformance();
  }

  @Override
  protected COUNTERItemUsage createReportItemForPeriod(
      COUNTERItemUsage reportItem, List<COUNTERItemPerformance> performanceOfPeriod) {
    return new COUNTERItemUsage()
        .item(reportItem.getItem())
        .itemID(modifiableCopyOf(reportItem.getItemID()))
        .itemContributors(modifiableCopyOf(reportItem.getItemContributors()))
        .itemDates(modifiableCopyOf(reportItem.getItemDates()))
        .itemAttributes(modifiableCopyOf(reportItem.getItemAttributes()))
        .platform(reportItem.getPlatform())
        .publisher(reportItem.getPublisher())
        .publisherID(modifiableCopyOf(reportItem.getPublisherID()))
        .itemParent(reportItem.getItemParent())
        .itemComponent(modifiableCopyOf(reportItem.getItemComponent()))
        .dataType(reportItem.getDataType())
        .YOP(reportItem.getYOP())
        .accessType(reportItem.getAccessType())
        .accessMethod(reportItem.getAccessMethod())
        .performance(performanceOfPeriod);
  }

  @Override
  protected COUNTERItemReport createReport(
      SUSHIReportHeader reportHeader, List<COUNTERItemUsage> reportItems) {
    return new COUNTERItemReport().reportHeader(reportHeader).reportItems(reportItems);
  }

  /**
   * Declared so that code compiled against earlier versions keeps linking: the method inherited
   * from {@link AbstractReportsSplitter} erases to {@code split(Object)} and would not provide
   * {@code split(COUNTERItemReport)}.
   */
  @SuppressWarnings("java:S1185") // not redundant: see javadoc above
  @Override
  public List<COUNTERItemReport> split(COUNTERItemReport report) {
    return super.split(report);
  }
}
