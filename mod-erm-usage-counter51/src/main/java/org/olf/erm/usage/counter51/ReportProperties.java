package org.olf.erm.usage.counter51;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.olf.erm.usage.counter51.JsonProperties.BEGIN_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.END_DATE;

import java.util.List;
import java.util.Map;
import org.openapitools.counter51client.model.DRReportAttributes;
import org.openapitools.counter51client.model.DRReportFilters;
import org.openapitools.counter51client.model.IRReportAttributes;
import org.openapitools.counter51client.model.IRReportFilters;
import org.openapitools.counter51client.model.ItemParentItem;
import org.openapitools.counter51client.model.ItemReportItem;
import org.openapitools.counter51client.model.PRReportAttributes;
import org.openapitools.counter51client.model.PRReportFilters;
import org.openapitools.counter51client.model.TRReportAttributes;
import org.openapitools.counter51client.model.TRReportAttributes.AttributesToShowEnum;
import org.openapitools.counter51client.model.TRReportFilters;
import org.openapitools.counter51client.model.TRReportFilters.AccessMethodEnum;
import org.openapitools.counter51client.model.TRReportFilters.AccessTypeEnum;
import org.openapitools.counter51client.model.TRReportFilters.DataTypeEnum;
import org.openapitools.counter51client.model.TRReportFilters.MetricTypeEnum;

enum ReportProperties {
  TR_J1(
      List.of(DataTypeEnum.JOURNAL.getValue()),
      List.of(AccessTypeEnum.CONTROLLED.getValue()),
      List.of(AccessMethodEnum.REGULAR.getValue()),
      List.of(
          MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          MetricTypeEnum.UNIQUE_ITEM_REQUESTS.getValue())),

  TR_J2(
      List.of(DataTypeEnum.JOURNAL.getValue()),
      emptyList(),
      List.of(AccessMethodEnum.REGULAR.getValue()),
      List.of(MetricTypeEnum.LIMIT_EXCEEDED.getValue(), MetricTypeEnum.NO_LICENSE.getValue())),

  TR_J3(
      List.of(DataTypeEnum.JOURNAL.getValue()),
      emptyList(),
      List.of(AccessMethodEnum.REGULAR.getValue()),
      List.of(
          MetricTypeEnum.TOTAL_ITEM_INVESTIGATIONS.getValue(),
          MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          MetricTypeEnum.UNIQUE_ITEM_INVESTIGATIONS.getValue(),
          MetricTypeEnum.UNIQUE_ITEM_REQUESTS.getValue()),
      List.of(TRReportFilters.JSON_PROPERTY_ACCESS_TYPE)),

  TR_J4(
      List.of(DataTypeEnum.JOURNAL.getValue()),
      List.of(AccessTypeEnum.CONTROLLED.getValue()),
      List.of(AccessMethodEnum.REGULAR.getValue()),
      List.of(
          MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          MetricTypeEnum.UNIQUE_ITEM_REQUESTS.getValue()),
      List.of(TRReportFilters.JSON_PROPERTY_Y_O_P)),

  TR_B1(
      List.of(DataTypeEnum.BOOK.getValue(), DataTypeEnum.REFERENCE_WORK.getValue()),
      List.of(AccessTypeEnum.CONTROLLED.getValue()),
      List.of(AccessMethodEnum.REGULAR.getValue()),
      List.of(
          MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          MetricTypeEnum.UNIQUE_TITLE_REQUESTS.getValue()),
      List.of(TRReportFilters.JSON_PROPERTY_DATA_TYPE, TRReportFilters.JSON_PROPERTY_Y_O_P)),

  TR_B2(
      List.of(DataTypeEnum.BOOK.getValue(), DataTypeEnum.REFERENCE_WORK.getValue()),
      emptyList(),
      List.of(AccessMethodEnum.REGULAR.getValue()),
      List.of(MetricTypeEnum.LIMIT_EXCEEDED.getValue(), MetricTypeEnum.NO_LICENSE.getValue()),
      List.of(TRReportFilters.JSON_PROPERTY_DATA_TYPE, TRReportFilters.JSON_PROPERTY_Y_O_P)),

  TR_B3(
      List.of(DataTypeEnum.BOOK.getValue(), DataTypeEnum.REFERENCE_WORK.getValue()),
      emptyList(),
      List.of(AccessMethodEnum.REGULAR.getValue()),
      List.of(
          MetricTypeEnum.TOTAL_ITEM_INVESTIGATIONS.getValue(),
          MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          MetricTypeEnum.UNIQUE_ITEM_INVESTIGATIONS.getValue(),
          MetricTypeEnum.UNIQUE_ITEM_REQUESTS.getValue(),
          MetricTypeEnum.UNIQUE_TITLE_INVESTIGATIONS.getValue(),
          MetricTypeEnum.UNIQUE_TITLE_REQUESTS.getValue()),
      List.of(
          TRReportFilters.JSON_PROPERTY_DATA_TYPE,
          TRReportFilters.JSON_PROPERTY_Y_O_P,
          TRReportFilters.JSON_PROPERTY_ACCESS_TYPE)),

  DR_D1(
      emptyList(),
      emptyList(),
      List.of(DRReportFilters.AccessMethodEnum.REGULAR.getValue()),
      List.of(
          DRReportFilters.MetricTypeEnum.SEARCHES_AUTOMATED.getValue(),
          DRReportFilters.MetricTypeEnum.SEARCHES_FEDERATED.getValue(),
          DRReportFilters.MetricTypeEnum.SEARCHES_REGULAR.getValue(),
          DRReportFilters.MetricTypeEnum.TOTAL_ITEM_INVESTIGATIONS.getValue(),
          DRReportFilters.MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          DRReportFilters.MetricTypeEnum.UNIQUE_ITEM_INVESTIGATIONS.getValue(),
          DRReportFilters.MetricTypeEnum.UNIQUE_ITEM_REQUESTS.getValue())),

  DR_D2(
      emptyList(),
      emptyList(),
      List.of(DRReportFilters.AccessMethodEnum.REGULAR.getValue()),
      List.of(
          DRReportFilters.MetricTypeEnum.LIMIT_EXCEEDED.getValue(),
          DRReportFilters.MetricTypeEnum.NO_LICENSE.getValue())),

  PR_P1(
      emptyList(),
      emptyList(),
      List.of(PRReportFilters.AccessMethodEnum.REGULAR.getValue()),
      List.of(
          PRReportFilters.MetricTypeEnum.SEARCHES_PLATFORM.getValue(),
          PRReportFilters.MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          PRReportFilters.MetricTypeEnum.UNIQUE_ITEM_REQUESTS.getValue(),
          PRReportFilters.MetricTypeEnum.UNIQUE_TITLE_REQUESTS.getValue()),
      List.of(PRReportFilters.JSON_PROPERTY_DATA_TYPE)),

  IR_A1(
      List.of(IRReportFilters.DataTypeEnum.ARTICLE.getValue()),
      emptyList(),
      List.of(IRReportFilters.AccessMethodEnum.REGULAR.getValue()),
      List.of(
          IRReportFilters.MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          IRReportFilters.MetricTypeEnum.UNIQUE_ITEM_REQUESTS.getValue()),
      List.of(IRReportFilters.JSON_PROPERTY_ACCESS_TYPE),
      emptyList(),
      List.of(
          ItemParentItem.JSON_PROPERTY_PUBLICATION_DATE, ItemParentItem.JSON_PROPERTY_DATA_TYPE)),

  IR_M1(
      List.of(
          IRReportFilters.DataTypeEnum.AUDIOVISUAL.getValue(),
          IRReportFilters.DataTypeEnum.IMAGE.getValue(),
          IRReportFilters.DataTypeEnum.INTERACTIVE_RESOURCE.getValue(),
          IRReportFilters.DataTypeEnum.MULTIMEDIA.getValue(),
          IRReportFilters.DataTypeEnum.SOUND.getValue()),
      emptyList(),
      List.of(IRReportFilters.AccessMethodEnum.REGULAR.getValue()),
      List.of(
          IRReportFilters.MetricTypeEnum.TOTAL_ITEM_REQUESTS.getValue(),
          IRReportFilters.MetricTypeEnum.UNIQUE_ITEM_REQUESTS.getValue()),
      List.of(IRReportFilters.JSON_PROPERTY_DATA_TYPE),
      List.of(
          ItemReportItem.JSON_PROPERTY_AUTHORS,
          ItemReportItem.JSON_PROPERTY_PUBLICATION_DATE,
          ItemReportItem.JSON_PROPERTY_ARTICLE_VERSION),
      List.of(
          ItemParentItem.JSON_PROPERTY_TITLE,
          ItemParentItem.JSON_PROPERTY_AUTHORS,
          ItemParentItem.JSON_PROPERTY_PUBLICATION_DATE,
          ItemParentItem.JSON_PROPERTY_ARTICLE_VERSION,
          ItemParentItem.JSON_PROPERTY_DATA_TYPE,
          ItemParentItem.JSON_PROPERTY_ITEM_I_D)),

  DR(
      Map.of(
          DRReportAttributes.JSON_PROPERTY_ATTRIBUTES_TO_SHOW,
          List.of(DRReportAttributes.AttributesToShowEnum.ACCESS_METHOD))),

  IR(
      Map.of(
          IRReportAttributes.JSON_PROPERTY_ATTRIBUTES_TO_SHOW,
          List.of(
              IRReportAttributes.AttributesToShowEnum.AUTHORS.getValue(),
              IRReportAttributes.AttributesToShowEnum.PUBLICATION_DATE.getValue(),
              IRReportAttributes.AttributesToShowEnum.ARTICLE_VERSION.getValue(),
              IRReportAttributes.AttributesToShowEnum.YOP.getValue(),
              IRReportAttributes.AttributesToShowEnum.ACCESS_TYPE.getValue(),
              IRReportAttributes.AttributesToShowEnum.ACCESS_METHOD.getValue()),
          IRReportAttributes.JSON_PROPERTY_INCLUDE_PARENT_DETAILS,
          "True")),

  PR(
      Map.of(
          PRReportAttributes.JSON_PROPERTY_ATTRIBUTES_TO_SHOW,
          List.of(PRReportAttributes.AttributesToShowEnum.ACCESS_METHOD))),

  TR(
      Map.of(
          TRReportAttributes.JSON_PROPERTY_ATTRIBUTES_TO_SHOW,
          List.of(
              AttributesToShowEnum.YOP.getValue(),
              AttributesToShowEnum.ACCESS_TYPE.getValue(),
              AttributesToShowEnum.ACCESS_METHOD.getValue())));

  private static final List<String> DEFAULT_REPORT_FILTERS = List.of(BEGIN_DATE, END_DATE);
  private final List<String> metricTypes;
  private final List<String> dataTypes;
  private final List<String> accessTypes;
  private final List<String> accessMethods;
  private final List<String> performanceAttributes;
  private final List<String> itemAttributesToRemove;
  private final List<String> parentItemAttributesToRemove;
  private final Map<String, Object> reportAttributes;

  ReportProperties(
      List<String> dataTypes,
      List<String> accessTypes,
      List<String> accessMethods,
      List<String> metricTypes,
      List<String> performanceAttributes,
      List<String> itemAttributesToRemove,
      List<String> parentItemAttributesToRemove) {
    this.dataTypes = dataTypes;
    this.accessTypes = accessTypes;
    this.accessMethods = accessMethods;
    this.metricTypes = metricTypes;
    this.performanceAttributes = performanceAttributes;
    this.itemAttributesToRemove = itemAttributesToRemove;
    this.parentItemAttributesToRemove = parentItemAttributesToRemove;
    this.reportAttributes = emptyMap();
  }

  ReportProperties(
      List<String> dataTypes,
      List<String> accessTypes,
      List<String> accessMethods,
      List<String> metricTypes,
      List<String> performanceAttributes) {
    this(
        dataTypes,
        accessTypes,
        accessMethods,
        metricTypes,
        performanceAttributes,
        emptyList(),
        emptyList());
  }

  ReportProperties(
      List<String> dataTypes,
      List<String> accessTypes,
      List<String> accessMethods,
      List<String> metricTypes) {
    this(dataTypes, accessTypes, accessMethods, metricTypes, emptyList(), emptyList(), emptyList());
  }

  ReportProperties(Map<String, Object> reportAttributes) {
    this.dataTypes = emptyList();
    this.accessTypes = emptyList();
    this.accessMethods = emptyList();
    this.metricTypes = emptyList();
    this.performanceAttributes = emptyList();
    this.itemAttributesToRemove = emptyList();
    this.parentItemAttributesToRemove = emptyList();
    this.reportAttributes = reportAttributes;
  }

  public List<String> getMetricTypes() {
    return metricTypes;
  }

  public List<String> getDataTypes() {
    return dataTypes;
  }

  public List<String> getAccessTypes() {
    return accessTypes;
  }

  public List<String> getAccessMethods() {
    return accessMethods;
  }

  public List<String> getPerformanceAttributes() {
    return performanceAttributes;
  }

  public List<String> getItemAttributesToRemove() {
    return itemAttributesToRemove;
  }

  public List<String> getParentItemAttributesToRemove() {
    return parentItemAttributesToRemove;
  }

  public Map<String, Object> getReportAttributes() {
    return reportAttributes;
  }

  public List<String> getRequiredReportFilters() {
    return DEFAULT_REPORT_FILTERS;
  }
}
