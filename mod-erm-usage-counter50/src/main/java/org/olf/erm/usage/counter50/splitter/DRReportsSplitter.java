package org.olf.erm.usage.counter50.splitter;

import io.vertx.core.json.Json;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
          COUNTERDatabaseReport clone =
              Json.decodeValue(Json.encode(report), COUNTERDatabaseReport.class);

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

          List<SUSHIReportHeaderReportFilters> reportFilters =
              clone.getReportHeader().getReportFilters();
          clone.getReportHeader().setReportFilters(replaceBeginAndEndDate(reportFilters, period));
          result.add(clone);
        });
    return result;
  }
}
