package org.olf.erm.usage.counter41.csv.mapper.report2csv;

import org.niso.schemas.counter.MetricType;

public enum Activity {
  SEARCH_REG("Regular Searches", MetricType.SEARCH_REG),
  SEARCH_FED("Searches-federated and automated", MetricType.SEARCH_FED),
  RESULT_CLICK("Result Clicks", MetricType.RESULT_CLICK),
  RECORD_VIEW("Record Views", MetricType.RECORD_VIEW);

  private final String text;
  private final MetricType metricType;

  Activity(String text, MetricType metricType) {
    this.text = text;
    this.metricType = metricType;
  }

  public String getText() {
    return text;
  }

  public MetricType getMetricType() {
    return metricType;
  }
}
