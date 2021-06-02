package org.olf.erm.usage.counter50.splitter;

import com.google.gson.Gson;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERDatabaseUsage;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformancePeriod;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

public class DRReportsSplitter extends AbstractReportsSplitter<COUNTERDatabaseReport> {

  @Override
  public List<COUNTERDatabaseReport> split(COUNTERDatabaseReport report) {
    List<YearMonth> yms = Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader());
    List<COUNTERDatabaseReport> result = new ArrayList<>();
    yms.forEach(
        ym -> {
          Gson gson = new Gson();
          COUNTERDatabaseReport clone =
              gson.fromJson(gson.toJson(report), COUNTERDatabaseReport.class);

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
              .map(COUNTERDatabaseUsage::getPerformance)
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
