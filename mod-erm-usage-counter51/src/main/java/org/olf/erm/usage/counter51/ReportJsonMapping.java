package org.olf.erm.usage.counter51;

import static org.olf.erm.usage.counter51.IdentifierNamespaces.PUBLISHER_IDENTIFIERS;
import static org.olf.erm.usage.counter51.JsonProperties.ARTICLE_VERSION;
import static org.olf.erm.usage.counter51.JsonProperties.ATTRIBUTE_PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.AUTHORS;
import static org.olf.erm.usage.counter51.JsonProperties.DATABASE;
import static org.olf.erm.usage.counter51.JsonProperties.ITEM;
import static org.olf.erm.usage.counter51.JsonProperties.ITEMS;
import static org.olf.erm.usage.counter51.JsonProperties.ITEM_ID;
import static org.olf.erm.usage.counter51.JsonProperties.PLATFORM;
import static org.olf.erm.usage.counter51.JsonProperties.PUBLICATION_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.PUBLISHER;
import static org.olf.erm.usage.counter51.JsonProperties.PUBLISHER_ID;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_ACCESS_METHOD;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_ACCESS_TYPE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS_DATA_TYPE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ITEMS;
import static org.olf.erm.usage.counter51.JsonProperties.TITLE;
import static org.olf.erm.usage.counter51.JsonProperties.YOP;
import static org.olf.erm.usage.counter51.ReportFieldProcessor.createAuthors;
import static org.olf.erm.usage.counter51.ReportFieldProcessor.createIdentifiers;
import static org.olf.erm.usage.counter51.ReportFieldProcessor.createItemId;

import java.util.List;

/**
 * Enumeration of mapping configurations for different COUNTER 5.1 report types. Each report type
 * (TR, IR, PR, DR) has its own specific mapping structure that defines how CSV data should be
 * transformed into the corresponding JSON representation.
 *
 * <p>The mapping includes column group definitions, header mappings, and specialized functions for
 * transforming data at different levels of the report structure.
 */
@SuppressWarnings("unchecked")
enum ReportJsonMapping {
  TR(
      ReportJsonMappingItem.defaultReportHeaderMappingItem(),
      List.of(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, new int[] {10, 11, 12, 13}),
      List.of(
          ReportJsonMappingItem.of(
              REPORT_ITEMS,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder()
                    .putRequired(TITLE, list.get(0))
                    .putRequired(PUBLISHER, list.get(1))
                    .putOptional(
                        PUBLISHER_ID, createIdentifiers(list.get(2), PUBLISHER_IDENTIFIERS))
                    .putRequired(PLATFORM, list.get(3))
                    .putOptional(
                        ITEM_ID,
                        createItemId(
                            list.get(4),
                            list.get(5),
                            list.get(6),
                            list.get(7),
                            list.get(8),
                            list.get(9)))
                    .build();
              }),
          ReportJsonMappingItem.of(
              ATTRIBUTE_PERFORMANCE,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder()
                    .putRequired(REPORT_FILTERS_DATA_TYPE, list.get(0))
                    .putOptional(YOP, list.get(1))
                    .putOptional(REPORT_FILTERS_ACCESS_TYPE, list.get(2))
                    .putOptional(REPORT_FILTERS_ACCESS_METHOD, list.get(3))
                    .build();
              }),
          ReportJsonMappingItem.defaultPerformanceMappingItem())),
  IR(
      ReportJsonMappingItem.defaultReportHeaderMappingItem(),
      List.of(
          new int[] {13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23},
          new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
          new int[] {24, 25, 26, 27}),
      List.of(
          ReportJsonMappingItem.of(
              REPORT_ITEMS,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder()
                    .putOptional(TITLE, list.get(0))
                    .putOptional(AUTHORS, createAuthors(list.get(1)))
                    .putOptional(PUBLICATION_DATE, list.get(2))
                    .putOptional(ARTICLE_VERSION, list.get(3))
                    .putOptional(REPORT_FILTERS_DATA_TYPE, list.get(4))
                    .putOptional(
                        ITEM_ID,
                        createItemId(
                            list.get(5),
                            list.get(6),
                            list.get(7),
                            list.get(8),
                            list.get(9),
                            list.get(10)))
                    .build();
              }),
          ReportJsonMappingItem.of(
              ITEMS,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder()
                    .putRequired(ITEM, list.get(0))
                    .putRequired(PUBLISHER, list.get(1))
                    .putOptional(
                        PUBLISHER_ID, createIdentifiers(list.get(2), PUBLISHER_IDENTIFIERS))
                    .putRequired(PLATFORM, list.get(3))
                    .putOptional(AUTHORS, createAuthors(list.get(4)))
                    .putOptional(PUBLICATION_DATE, list.get(5))
                    .putOptional(ARTICLE_VERSION, list.get(6))
                    .putOptional(
                        ITEM_ID,
                        createItemId(
                            list.get(7),
                            list.get(8),
                            list.get(9),
                            list.get(10),
                            list.get(11),
                            list.get(12)))
                    .build();
              }),
          ReportJsonMappingItem.of(
              ATTRIBUTE_PERFORMANCE,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder()
                    .putRequired(REPORT_FILTERS_DATA_TYPE, list.get(0))
                    .putOptional(YOP, list.get(1))
                    .putOptional(REPORT_FILTERS_ACCESS_TYPE, list.get(2))
                    .putOptional(REPORT_FILTERS_ACCESS_METHOD, list.get(3))
                    .build();
              }),
          ReportJsonMappingItem.defaultPerformanceMappingItem())),
  PR(
      ReportJsonMappingItem.defaultReportHeaderMappingItem(),
      List.of(new int[] {0}, new int[] {1, 2}),
      List.of(
          ReportJsonMappingItem.of(
              REPORT_ITEMS,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder().putRequired(PLATFORM, list.get(0)).build();
              }),
          ReportJsonMappingItem.of(
              ATTRIBUTE_PERFORMANCE,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder()
                    .putRequired(REPORT_FILTERS_DATA_TYPE, list.get(0))
                    .putOptional(REPORT_FILTERS_ACCESS_METHOD, list.get(1))
                    .build();
              }),
          ReportJsonMappingItem.defaultPerformanceMappingItem())),
  DR(
      ReportJsonMappingItem.defaultReportHeaderMappingItem(),
      List.of(new int[] {0, 1, 2, 3, 4}, new int[] {5, 6}),
      List.of(
          ReportJsonMappingItem.of(
              REPORT_ITEMS,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder()
                    .putRequired(DATABASE, list.get(0))
                    .putRequired(PUBLISHER, list.get(1))
                    .putOptional(
                        PUBLISHER_ID, createIdentifiers(list.get(2), PUBLISHER_IDENTIFIERS))
                    .putRequired(PLATFORM, list.get(3))
                    .putOptional(ITEM_ID, createItemId(list.get(4)))
                    .build();
              }),
          ReportJsonMappingItem.of(
              ATTRIBUTE_PERFORMANCE,
              data -> {
                List<String> list = (List<String>) data;
                return new NodeBuilder()
                    .putRequired(REPORT_FILTERS_DATA_TYPE, list.get(0))
                    .putOptional(REPORT_FILTERS_ACCESS_METHOD, list.get(1))
                    .build();
              }),
          ReportJsonMappingItem.defaultPerformanceMappingItem()));

  private final ReportJsonMappingItem headerMapping;
  private final List<int[]> columnGroups;
  private final List<ReportJsonMappingItem> columnGroupMappings;

  public ReportJsonMappingItem getHeaderMapping() {
    return headerMapping;
  }

  public List<int[]> getColumnGroups() {
    return columnGroups;
  }

  public List<ReportJsonMappingItem> getColumnGroupMappings() {
    return columnGroupMappings;
  }

  ReportJsonMapping(
      ReportJsonMappingItem headerMapping,
      List<int[]> columnGroups,
      List<ReportJsonMappingItem> columnGroupMappings) {
    this.headerMapping = headerMapping;
    this.columnGroups = columnGroups;
    this.columnGroupMappings = columnGroupMappings;
  }
}
