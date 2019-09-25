package org.olf.erm.usage.counter41.csv.mapper;

import org.niso.schemas.counter.Category;
import org.niso.schemas.counter.MetricType;

public enum Activity {
  SEARCH_REG("Regular Searches", MetricType.SEARCH_REG, Category.SEARCHES),
  SEARCH_FED("Searches-federated and automated", MetricType.SEARCH_FED, Category.SEARCHES),
  RESULT_CLICK("Result Clicks", MetricType.RESULT_CLICK, Category.REQUESTS),
  RECORD_VIEW("Record Views", MetricType.RECORD_VIEW, Category.REQUESTS);

  private final String text;
  private final MetricType metricType;
  private final Category category;

  Activity(String text, MetricType metricType, Category category) {
    this.text = text;
    this.metricType = metricType;
    this.category = category;
  }

  public String getText() {
    return text;
  }

  public MetricType getMetricType() {
    return metricType;
  }

  public Category getCategory() {
    return category;
  }
}
