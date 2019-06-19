package org.olf.erm.usage.counter41.csv.cellprocessor;

import java.math.BigInteger;
import java.time.YearMonth;
import java.util.ArrayList;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.PerformanceCounter;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class MonthPerformanceProcessor extends CellProcessorAdaptor {

  YearMonth yearMonth;
  MetricType metricType;

  public MonthPerformanceProcessor(YearMonth yearMonth, MetricType metricType) {
    this.yearMonth = yearMonth;
    this.metricType = metricType;
  }

  @SuppressWarnings("unchecked")
  @Override
  public BigInteger execute(Object value, CsvContext context) {
    return ((ArrayList<Metric>) value)
        .stream()
            .filter(
                m ->
                    m.getPeriod()
                            .getBegin()
                            .toGregorianCalendar()
                            .toZonedDateTime()
                            .toLocalDate()
                            .equals(yearMonth.atDay(1))
                        && m.getPeriod()
                            .getEnd()
                            .toGregorianCalendar()
                            .toZonedDateTime()
                            .toLocalDate()
                            .equals(yearMonth.atEndOfMonth()))
            .flatMap(m -> m.getInstance().stream())
            .filter(pc -> pc.getMetricType().equals(metricType))
            .map(PerformanceCounter::getCount)
            .reduce(BigInteger::add)
            .orElse(null);
  }
}
