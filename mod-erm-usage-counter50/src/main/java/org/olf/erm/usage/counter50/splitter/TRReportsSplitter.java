package org.olf.erm.usage.counter50.splitter;

import io.vertx.core.json.Json;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformancePeriod;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

public class TRReportsSplitter extends AbstractReportsSplitter<COUNTERTitleReport> {

  @Override
  public List<COUNTERTitleReport> split(COUNTERTitleReport report) {
    List<YearMonth> yms = Counter5Utils.getYearMonthsFromReportHeader(report.getReportHeader());
    List<COUNTERTitleReport> result = new ArrayList<>();
    yms.forEach(
        ym -> {
          COUNTERTitleReport clone =
              Json.decodeValue(Json.encode(report), COUNTERTitleReport.class);

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
              .map(COUNTERTitleUsage::getPerformance)
              .forEach(list -> list.removeIf(metric -> !metric.getPeriod().equals(period)));

          List<SUSHIReportHeaderReportFilters> reportFilters =
              clone.getReportHeader().getReportFilters();
          clone.getReportHeader().setReportFilters(replaceBeginAndEndDate(reportFilters, period));
          result.add(clone);
        });
    return result;
  }
}
