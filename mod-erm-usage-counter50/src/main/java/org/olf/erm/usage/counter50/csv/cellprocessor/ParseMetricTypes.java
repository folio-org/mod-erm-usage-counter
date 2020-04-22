package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.COUNTERItemPerformancePeriod;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseMetricTypes extends CellProcessorAdaptor {

  private final String[] header;
  private int metricTypeIndex = -1;

  public ParseMetricTypes(String[] header) {
    this.header = Arrays.copyOf(header, header.length);

    for (int i = 0; i < header.length; i++) {
      String h = header[i];
      if (h.equalsIgnoreCase("Metric_Type")) {
        metricTypeIndex = i;
      }
    }
  }

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }

    String yearMonthString = header[csvContext.getColumnNumber() - 1];
    String metricTypeString = (String) csvContext.getRowSource().get(metricTypeIndex);

    COUNTERItemPerformance counterItemPerformance = new COUNTERItemPerformance();
    COUNTERItemPerformancePeriod period = new COUNTERItemPerformancePeriod();
    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("uuuu-MM", Locale.ENGLISH);
    YearMonth ym = YearMonth.parse(yearMonthString, formatter);
    LocalDate beginDate = ym.atDay(1);
    LocalDate endDate = ym.atEndOfMonth();
    period.setBeginDate(beginDate.format(DateTimeFormatter.ISO_DATE));
    period.setEndDate(endDate.format(DateTimeFormatter.ISO_DATE));
    counterItemPerformance.setPeriod(period);

    COUNTERItemPerformanceInstance instance = new COUNTERItemPerformanceInstance();
    instance.setCount(Integer.valueOf((String) value));
    instance.setMetricType(MetricTypeEnum.fromValue(metricTypeString));
    counterItemPerformance.addInstanceItem(instance);
    return counterItemPerformance;
  }
}
