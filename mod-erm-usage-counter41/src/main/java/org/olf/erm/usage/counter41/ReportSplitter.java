package org.olf.erm.usage.counter41;

import static org.olf.erm.usage.counter41.Counter4Utils.getYearMonthsFromReport;

import java.util.ArrayList;
import java.util.List;
import org.niso.schemas.counter.DateRange;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.Counter4Utils.ReportSplitException;

/**
 * Splits a COUNTER 4.1 report into one report per month covered by it.
 *
 * <p>A metric belongs to exactly one month, so a month's report can be assembled from the metrics
 * of that month alone.
 */
class ReportSplitter {

  /**
   * Splits a report that spans multiple months into a list of reports spanning one month each.
   *
   * @param report the report to split
   * @return an unmodifiable list holding one report per month, in chronological order
   * @throws ReportSplitException if the report does not hold exactly one customer
   */
  public List<Report> split(Report report) throws ReportSplitException {
    validateSingleCustomer(report);
    return getDateRangesCoveredBy(report).stream()
        .map(dateRange -> createReportForDateRange(report, dateRange))
        .toList();
  }

  private void validateSingleCustomer(Report report) throws ReportSplitException {
    if (report.getCustomer().isEmpty()) {
      throw new ReportSplitException("Report contains no customer");
    }
    if (report.getCustomer().size() > 1) {
      throw new ReportSplitException("Report contains multiple customer entries");
    }
  }

  /** Returns one date range per month covered by the report, in chronological order. */
  private List<DateRange> getDateRangesCoveredBy(Report report) {
    return getYearMonthsFromReport(report).stream()
        .map(Counter4Utils::getDateRangeForYearMonth)
        .toList();
  }

  /** Creates the report holding the usage of a single month. */
  private Report createReportForDateRange(Report report, DateRange dateRange) {
    Report reportForDateRange = createReportWithoutCustomer(report);
    reportForDateRange
        .getCustomer()
        .add(createCustomerForDateRange(report.getCustomer().get(0), dateRange));
    return reportForDateRange;
  }

  private Report createReportWithoutCustomer(Report report) {
    Report reportCopy = new Report();
    reportCopy.setVendor(report.getVendor());
    reportCopy.setCreated(report.getCreated());
    reportCopy.setID(report.getID());
    reportCopy.setVersion(report.getVersion());
    reportCopy.setName(report.getName());
    reportCopy.setTitle(report.getTitle());
    return reportCopy;
  }

  private Report.Customer createCustomerForDateRange(
      Report.Customer customer, DateRange dateRange) {
    Report.Customer customerCopy = new Report.Customer();
    customerCopy.setName(customer.getName());
    customerCopy.setID(customer.getID());
    customerCopy.setContact(modifiableCopyOf(customer.getContact()));
    customerCopy.setWebSiteUrl(customer.getWebSiteUrl());
    customerCopy.setLogoUrl(customer.getLogoUrl());
    customerCopy.setConsortium(customer.getConsortium());
    customerCopy.setInstitutionalIdentifier(
        modifiableCopyOf(customer.getInstitutionalIdentifier()));
    customerCopy.setReportItems(getReportItemsForDateRange(customer, dateRange));
    return customerCopy;
  }

  /**
   * Returns one report item per source item that holds usage for the given month, in the order of
   * the source report. An item without usage for that month is left out.
   */
  private List<ReportItem> getReportItemsForDateRange(
      Report.Customer customer, DateRange dateRange) {
    List<ReportItem> reportItemsForDateRange = new ArrayList<>();
    for (ReportItem reportItem : customer.getReportItems()) {
      List<Metric> metricsOfDateRange = getMetricsOfDateRange(reportItem, dateRange);
      if (!metricsOfDateRange.isEmpty()) {
        reportItemsForDateRange.add(createReportItemForDateRange(reportItem, metricsOfDateRange));
      }
    }
    return reportItemsForDateRange;
  }

  /** Returns those metrics of the report item that belong to the given month. */
  private List<Metric> getMetricsOfDateRange(ReportItem reportItem, DateRange dateRange) {
    List<Metric> metricsOfDateRange = new ArrayList<>();
    for (Metric metric : reportItem.getItemPerformance()) {
      if (dateRange.equals(metric.getPeriod())) {
        metricsOfDateRange.add(metric);
      }
    }
    return metricsOfDateRange;
  }

  /**
   * Creates a report item that holds the attributes of {@code reportItem} and, as its only usage,
   * the metrics of a single month.
   *
   * <p>Every attribute has to be passed on; a forgotten one is silently dropped from the split
   * reports. {@code SplitPreservesReportTest} fails when one is left out, and also when a
   * regenerated model gains one.
   */
  private ReportItem createReportItemForDateRange(
      ReportItem reportItem, List<Metric> metricsOfDateRange) {
    ReportItem reportItemCopy = new ReportItem();
    reportItemCopy.setParentItem(reportItem.getParentItem());
    reportItemCopy.setItemPlatform(reportItem.getItemPlatform());
    reportItemCopy.setItemPublisher(reportItem.getItemPublisher());
    reportItemCopy.setItemName(reportItem.getItemName());
    reportItemCopy.setItemDataType(reportItem.getItemDataType());
    reportItemCopy.setItemIdentifier(modifiableCopyOf(reportItem.getItemIdentifier()));
    reportItemCopy.setItemContributor(modifiableCopyOf(reportItem.getItemContributor()));
    reportItemCopy.setItemDate(modifiableCopyOf(reportItem.getItemDate()));
    reportItemCopy.setItemAttribute(modifiableCopyOf(reportItem.getItemAttribute()));
    reportItemCopy.setItemPerformance(metricsOfDateRange);
    return reportItemCopy;
  }

  /**
   * Returns a modifiable copy of the given list.
   *
   * <p>The generated model initialises a list on first access rather than returning {@code null},
   * so no null check is needed. That also means reading an unset list assigns an empty one to the
   * source report, which no caller can tell apart from the unset list it replaces.
   */
  private <E> List<E> modifiableCopyOf(List<E> list) {
    return new ArrayList<>(list);
  }
}
