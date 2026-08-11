package org.olf.erm.usage.counter50.splitter;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERItemPerformancePeriod;
import org.openapitools.counter50.model.SUSHIReportHeader;
import org.openapitools.counter50.model.SUSHIReportHeaderReportFilters;

/**
 * Splits a COUNTER 5.0 report into one report per month covered by its header.
 *
 * <p>A performance entry belongs to exactly one month, so a month's report can be assembled from
 * the entries of that month alone.
 *
 * <p>Subclasses supply the report and report item types and how to read and create them.
 *
 * @param <T> the report type
 * @param <U> the report item type
 */
public abstract class AbstractReportsSplitter<T, U> {

  /** Returns the header of the given report. */
  protected abstract SUSHIReportHeader getReportHeader(T report);

  /** Returns the report items of the given report. */
  protected abstract List<U> getReportItems(T report);

  /** Returns the performance entries of the given report item. */
  protected abstract List<COUNTERItemPerformance> getPerformance(U reportItem);

  /**
   * Creates a report item that holds the attributes of {@code reportItem} and, as its only usage,
   * the performance entries of a single month.
   *
   * <p>Implementations must pass on every attribute of {@code reportItem}; a forgotten one is
   * silently dropped from the split reports. {@code SplitPreservesReportItemTest} fails when an
   * implementation drops one, and also when a regenerated model gains one.
   */
  protected abstract U createReportItemForPeriod(
      U reportItem, List<COUNTERItemPerformance> performanceOfPeriod);

  /** Creates a report from the given header and report items. */
  protected abstract T createReport(SUSHIReportHeader reportHeader, List<U> reportItems);

  /**
   * Splits the given report into one report per month covered by its header.
   *
   * @param report the report to split
   * @return an unmodifiable list holding one report per month, in chronological order
   */
  public List<T> split(T report) {
    return getPeriodsCoveredBy(report).stream()
        .map(period -> createReportForPeriod(report, period))
        .toList();
  }

  /** Returns one period per month covered by the report's header, in chronological order. */
  private List<COUNTERItemPerformancePeriod> getPeriodsCoveredBy(T report) {
    return Counter5Utils.getYearMonthsFromReportHeader(getReportHeader(report)).stream()
        .map(this::createPeriod)
        .toList();
  }

  /** Returns the period spanning the whole of the given month. */
  private COUNTERItemPerformancePeriod createPeriod(YearMonth yearMonth) {
    COUNTERItemPerformancePeriod period = new COUNTERItemPerformancePeriod();
    period.setBeginDate(yearMonth.atDay(1).format(DateTimeFormatter.ISO_DATE));
    period.setEndDate(yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_DATE));
    return period;
  }

  /** Creates the report holding the usage of a single month. */
  private T createReportForPeriod(T report, COUNTERItemPerformancePeriod period) {
    return createReport(
        createHeaderForPeriod(getReportHeader(report), period),
        getReportItemsForPeriod(report, period));
  }

  /**
   * Returns one report item per source item that holds usage for the given month, in the order of
   * the source report. An item without usage for that month is left out.
   */
  private List<U> getReportItemsForPeriod(T report, COUNTERItemPerformancePeriod period) {
    List<U> reportItemsForPeriod = new ArrayList<>();
    for (U reportItem : getReportItems(report)) {
      List<COUNTERItemPerformance> performanceOfPeriod = getPerformanceOfPeriod(reportItem, period);
      if (!performanceOfPeriod.isEmpty()) {
        reportItemsForPeriod.add(createReportItemForPeriod(reportItem, performanceOfPeriod));
      }
    }
    return reportItemsForPeriod;
  }

  /** Returns those performance entries of the report item that belong to the given month. */
  private List<COUNTERItemPerformance> getPerformanceOfPeriod(
      U reportItem, COUNTERItemPerformancePeriod period) {
    List<COUNTERItemPerformance> performanceOfPeriod = new ArrayList<>();
    for (COUNTERItemPerformance performance : getPerformance(reportItem)) {
      if (period.equals(performance.getPeriod())) {
        performanceOfPeriod.add(performance);
      }
    }
    return performanceOfPeriod;
  }

  /**
   * Copies the header, narrowing its Begin_Date and End_Date to the given month.
   *
   * <p>Every property has to be passed on; a forgotten one is silently dropped from the split
   * reports. {@code SplitPreservesReportItemTest} fails when one is, and also when a regenerated
   * model gains one.
   */
  private SUSHIReportHeader createHeaderForPeriod(
      SUSHIReportHeader reportHeader, COUNTERItemPerformancePeriod period) {
    return new SUSHIReportHeader()
        .created(reportHeader.getCreated())
        .createdBy(reportHeader.getCreatedBy())
        .customerID(reportHeader.getCustomerID())
        .reportID(reportHeader.getReportID())
        .release(reportHeader.getRelease())
        .reportName(reportHeader.getReportName())
        .institutionName(reportHeader.getInstitutionName())
        .institutionID(modifiableCopyOf(reportHeader.getInstitutionID()))
        .reportAttributes(modifiableCopyOf(reportHeader.getReportAttributes()))
        .exceptions(modifiableCopyOf(reportHeader.getExceptions()))
        .reportFilters(createReportFiltersForPeriod(reportHeader.getReportFilters(), period));
  }

  /**
   * Returns the header's filters with Begin_Date and End_Date set to the given month.
   *
   * <p>A header without filters gets a list holding just those two. They state which month a split
   * report covers, so unlike the other lists of the header they cannot stay absent.
   */
  private List<SUSHIReportHeaderReportFilters> createReportFiltersForPeriod(
      List<SUSHIReportHeaderReportFilters> reportFilters, COUNTERItemPerformancePeriod period) {
    List<SUSHIReportHeaderReportFilters> filtersOfPeriod =
        (reportFilters == null) ? new ArrayList<>() : modifiableCopyOf(reportFilters);
    return replaceBeginAndEndDate(filtersOfPeriod, period);
  }

  /**
   * Returns a modifiable copy of the given list, or {@code null} for a {@code null} list so that an
   * absent property stays absent.
   */
  protected static <E> List<E> modifiableCopyOf(List<E> list) {
    return (list == null) ? null : new ArrayList<>(list);
  }

  /**
   * Replaces header's Begin_Date and End_Date attributes. Replacing happens inline.
   *
   * @param reportFilters The {@link SUSHIReportHeaderReportFilters} in which the Begin_Date and
   *     End_Date shall be replaced
   * @param performancePeriod A {@link COUNTERItemPerformancePeriod} containing the new Begin_Date
   *     and End_Date
   * @return the given list, with Begin_Date and End_Date replaced
   * @throws NullPointerException if {@code reportFilters} is {@code null}
   */
  protected List<SUSHIReportHeaderReportFilters> replaceBeginAndEndDate(
      List<SUSHIReportHeaderReportFilters> reportFilters,
      COUNTERItemPerformancePeriod performancePeriod) {
    reportFilters.removeIf(
        repFilter ->
            "Begin_Date".equalsIgnoreCase(repFilter.getName())
                || "End_Date".equalsIgnoreCase(repFilter.getName()));

    SUSHIReportHeaderReportFilters beginFilter = new SUSHIReportHeaderReportFilters();
    beginFilter.setName("Begin_Date");
    beginFilter.setValue(performancePeriod.getBeginDate());
    reportFilters.add(beginFilter);

    SUSHIReportHeaderReportFilters endFilter = new SUSHIReportHeaderReportFilters();
    endFilter.setName("End_Date");
    endFilter.setValue(performancePeriod.getEndDate());
    reportFilters.add(endFilter);
    return reportFilters;
  }
}
