package org.olf.erm.usage.counter50.splitter;

import com.google.gson.Gson;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformancePeriod;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERPlatformUsage;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

public class PRReportsSplitter extends AbstractReportsSplitter<COUNTERPlatformReport> {

  public List<COUNTERPlatformReport> split(COUNTERPlatformReport report) {
    List<YearMonth> yms = Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader());
    List<COUNTERPlatformReport> result = new ArrayList<>();
    yms.forEach(
        ym -> {
          Gson gson = new Gson();
          COUNTERPlatformReport clone =
              gson.fromJson(gson.toJson(report), COUNTERPlatformReport.class);

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

          Optional<COUNTERItemPerformancePeriod> performance =
              clone.getReportItems().stream()
                  .flatMap(
                      counterTitleUsage ->
                          counterTitleUsage.getPerformance().stream()
                              .map(COUNTERItemPerformance::getPeriod))
                  .findFirst();
          List<SUSHIReportHeaderReportFilters> reportFilters =
              clone.getReportHeader().getReportFilters();
          performance.ifPresent(
              performancePeriod ->
                  clone
                      .getReportHeader()
                      .setReportFilters(replaceBeginAndEndDate(reportFilters, performancePeriod)));
          result.add(clone);
        });
    return result;
  }
}
