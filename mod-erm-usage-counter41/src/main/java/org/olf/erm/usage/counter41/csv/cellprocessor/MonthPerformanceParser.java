package org.olf.erm.usage.counter41.csv.cellprocessor;

import static org.olf.erm.usage.counter41.Counter4Utils.getDateRangeForYearMonth;

import java.math.BigInteger;
import java.time.YearMonth;
import org.niso.schemas.counter.Category;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.PerformanceCounter;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class MonthPerformanceParser extends CellProcessorAdaptor {

  private final YearMonth month;
  private final MetricType metricType;
  private final Category category;

  public MonthPerformanceParser(YearMonth month, MetricType metricType, Category category) {
    this.month = month;
    this.metricType = metricType;
    this.category = category;
  }

  @Override
  public Metric execute(Object value, CsvContext context) {
    if (value == null) {
      return null;
    }
    Metric metric = new Metric();
    metric.setPeriod(getDateRangeForYearMonth(month));
    metric.setCategory(category);
    PerformanceCounter pc = new PerformanceCounter();
    pc.setMetricType(metricType);
    pc.setCount(new BigInteger((String) value));
    metric.getInstance().add(pc);
    return metric;
  }
}
