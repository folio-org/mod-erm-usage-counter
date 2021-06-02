package org.olf.erm.usage.counter50.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olf.erm.usage.counter50.converter.ConverterUtils.createReportFiltersFromMap;
import static org.olf.erm.usage.counter50.converter.ConverterUtils.getMetricTypeCount;
import static org.olf.erm.usage.counter50.converter.ConverterUtils.mergeReportFilters;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.client.model.COUNTERItemPerformancePeriod;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

public class ConverterUtilsTest {

  @Test
  public void testGetMetricTypeCount() {
    COUNTERItemPerformancePeriod period =
        new COUNTERItemPerformancePeriod().beginDate("2021-01-01").endDate("2021-01-31");
    COUNTERItemPerformance p =
        new COUNTERItemPerformance()
            .period(period)
            .instance(
                List.of(
                    new COUNTERItemPerformanceInstance()
                        .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                        .count(5),
                    new COUNTERItemPerformanceInstance()
                        .metricType(MetricTypeEnum.TOTAL_ITEM_INVESTIGATIONS)
                        .count(7)));
    assertThat(getMetricTypeCount(p, MetricTypeEnum.TOTAL_ITEM_REQUESTS)).isEqualTo(5);
    assertThat(getMetricTypeCount(p, MetricTypeEnum.TOTAL_ITEM_INVESTIGATIONS)).isEqualTo(7);
    assertThat(getMetricTypeCount(p, MetricTypeEnum.UNIQUE_ITEM_INVESTIGATIONS)).isZero();
  }

  @Test
  public void testSumCOUNTERItemPerformanceEmptyInstances() {
    COUNTERItemPerformancePeriod period1 =
        new COUNTERItemPerformancePeriod().beginDate("2021-01-01").endDate("2021-01-31");
    COUNTERItemPerformance ip1 =
        new COUNTERItemPerformance().period(period1).instance(Collections.emptyList());
    COUNTERItemPerformance ip2 =
        new COUNTERItemPerformance().period(period1).instance(Collections.emptyList());
    List<COUNTERItemPerformance> result =
        ConverterUtils.sumCOUNTERItemPerformance(List.of(ip1, ip2));
    assertThat(result).isEmpty();
  }

  @Test
  public void testSumCOUNTERItemPerformance() {
    COUNTERItemPerformancePeriod period1 =
        new COUNTERItemPerformancePeriod().beginDate("2021-01-01").endDate("2021-01-31");
    COUNTERItemPerformancePeriod period2 =
        new COUNTERItemPerformancePeriod().beginDate("2021-02-01").endDate("2021-02-28");
    COUNTERItemPerformance ip1 =
        new COUNTERItemPerformance()
            .period(period1)
            .instance(
                List.of(
                    new COUNTERItemPerformanceInstance()
                        .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                        .count(1)));
    COUNTERItemPerformance ip2 =
        new COUNTERItemPerformance()
            .period(period1)
            .instance(
                List.of(
                    new COUNTERItemPerformanceInstance()
                        .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                        .count(2)));
    COUNTERItemPerformance ip3 =
        new COUNTERItemPerformance()
            .period(period2)
            .instance(
                List.of(
                    new COUNTERItemPerformanceInstance()
                        .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                        .count(5)));
    COUNTERItemPerformance ip4 =
        new COUNTERItemPerformance()
            .period(period2)
            .instance(
                List.of(
                    new COUNTERItemPerformanceInstance()
                        .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                        .count(7)));
    List<COUNTERItemPerformance> result =
        ConverterUtils.sumCOUNTERItemPerformance(List.of(ip1, ip2));
    assertThat(result)
        .hasSize(1)
        .containsExactly(
            new COUNTERItemPerformance()
                .period(period1)
                .instance(
                    List.of(
                        new COUNTERItemPerformanceInstance()
                            .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                            .count(3))));

    List<COUNTERItemPerformance> result2 =
        ConverterUtils.sumCOUNTERItemPerformance(List.of(ip1, ip2, ip3, ip4));
    assertThat(result2)
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new COUNTERItemPerformance()
                .period(period1)
                .instance(
                    List.of(
                        new COUNTERItemPerformanceInstance()
                            .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                            .count(3))),
            new COUNTERItemPerformance()
                .period(period2)
                .instance(
                    List.of(
                        new COUNTERItemPerformanceInstance()
                            .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                            .count(12))));

    List<COUNTERItemPerformance> result3 = ConverterUtils.sumCOUNTERItemPerformance(List.of(ip1));
    assertThat(result3)
        .hasSize(1)
        .containsExactly(
            new COUNTERItemPerformance()
                .period(period1)
                .instance(
                    List.of(
                        new COUNTERItemPerformanceInstance()
                            .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
                            .count(1))));
  }

  @Test
  public void testMergeReportFilters() {
    List<SUSHIReportHeaderReportFilters> filters1 =
        createReportFiltersFromMap(Map.of("key1", "value1", "key3", "value3"));
    List<SUSHIReportHeaderReportFilters> filters2 =
        createReportFiltersFromMap(Map.of("key1", "value2", "key2", "value2"));

    List<SUSHIReportHeaderReportFilters> result = mergeReportFilters(filters1, filters2);
    assertThat(result)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new SUSHIReportHeaderReportFilters().name("key1").value("value1"),
            new SUSHIReportHeaderReportFilters().name("key2").value("value2"),
            new SUSHIReportHeaderReportFilters().name("key3").value("value3"));
  }
}
