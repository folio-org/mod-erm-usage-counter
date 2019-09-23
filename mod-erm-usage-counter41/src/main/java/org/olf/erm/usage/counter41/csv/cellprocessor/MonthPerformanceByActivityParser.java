package org.olf.erm.usage.counter41.csv.cellprocessor;

import static org.olf.erm.usage.counter41.Counter4Utils.getDateRangeForYearMonth;

import java.math.BigInteger;
import java.time.YearMonth;
import java.util.Arrays;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.PerformanceCounter;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.Activity;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class MonthPerformanceByActivityParser extends CellProcessorAdaptor {

  private final YearMonth month;
  private final int activityColumn;

  public MonthPerformanceByActivityParser(YearMonth month, int activityColumn) {
    this.month = month;
    this.activityColumn = activityColumn;
  }

  @Override
  public Metric execute(Object value, CsvContext context) {
    if (value == null) {
      return null;
    }

    String activity = (String) context.getRowSource().get(activityColumn);
    return Arrays.stream(Activity.values())
        .filter(act -> act.getText().equals(activity))
        .findFirst()
        .map(
            act -> {
              Metric metric = new Metric();
              metric.setPeriod(getDateRangeForYearMonth(month));
              metric.setCategory(act.getCategory());
              PerformanceCounter pc = new PerformanceCounter();
              pc.setMetricType(act.getMetricType());
              pc.setCount(new BigInteger((String) value));
              metric.getInstance().add(pc);
              return metric;
            })
        .orElse(null);
  }
}
