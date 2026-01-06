package org.olf.erm.usage.counter50.splitter;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERItemPerformancePeriod;
import org.openapitools.counter50.model.COUNTERPlatformReport;
import org.openapitools.counter50.model.COUNTERPlatformUsage;
import org.openapitools.counter50.model.SUSHIReportHeaderReportFilters;

public class PRReportsSplitter extends AbstractReportsSplitter<COUNTERPlatformReport> {

  public List<COUNTERPlatformReport> split(COUNTERPlatformReport report) {
    List<YearMonth> yms = Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader());
    List<COUNTERPlatformReport> result = new ArrayList<>();
    yms.forEach(
        ym -> {
          COUNTERPlatformReport clone =
              Counter5Utils.getDefaultObjectMapper()
                  .convertValue(report, COUNTERPlatformReport.class);

          COUNTERItemPerformancePeriod period = new COUNTERItemPerformancePeriod();
          period.setBeginDate(ym.atDay(1).format(DateTimeFormatter.ISO_DATE));
          period.setEndDate(ym.atEndOfMonth().format(DateTimeFormatter.ISO_DATE));

          clone
              .getReportItems()
              .removeIf(
                  dbUsage ->
                      dbUsage.getPerformance().stream()
                          .map(COUNTERItemPerformance::getPeriod)
                          .noneMatch(p -> p.equals(period)));

          clone.getReportItems().stream()
              .map(COUNTERPlatformUsage::getPerformance)
              .forEach(list -> list.removeIf(metric -> !metric.getPeriod().equals(period)));

          List<SUSHIReportHeaderReportFilters> reportFilters =
              clone.getReportHeader().getReportFilters();
          clone.getReportHeader().setReportFilters(replaceBeginAndEndDate(reportFilters, period));
          result.add(clone);
        });
    return result;
  }
}
