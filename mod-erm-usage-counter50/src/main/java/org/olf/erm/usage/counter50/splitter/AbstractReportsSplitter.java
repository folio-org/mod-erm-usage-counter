package org.olf.erm.usage.counter50.splitter;

import java.util.List;
import org.openapitools.client.model.COUNTERItemPerformancePeriod;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

public abstract class AbstractReportsSplitter<T> {

  public abstract List<T> split(T report);

  /**
   * Replaces header's Begin_Date and End_Date attributes. Replacing happens inline.
   *
   * @param reportFilters The {@link SUSHIReportHeaderReportFilters} in which the Begin_Date and
   *     End_Date shall be replaced
   * @param performancePeriod A {@link COUNTERItemPerformancePeriod} containing the new Begin_Date
   *     and End_Date
   * @return
   */
  protected List<SUSHIReportHeaderReportFilters> replaceBeginAndEndDate(
      List<SUSHIReportHeaderReportFilters> reportFilters,
      COUNTERItemPerformancePeriod performancePeriod) {
    reportFilters.removeIf(
        repFilter ->
            repFilter.getName().equalsIgnoreCase("Begin_Date")
                || repFilter.getName().equalsIgnoreCase("End_Date"));

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
