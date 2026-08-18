package org.olf.erm.usage.counter51;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.olf.erm.usage.counter51.Counter51Utils.mergeReports;
import static org.olf.erm.usage.counter51.Counter51Utils.splitReport;
import static org.olf.erm.usage.counter51.Counter51Utils.writeReportAsCsv;
import static org.olf.erm.usage.counter51.JsonProperties.ATTRIBUTES_TO_SHOW;
import static org.olf.erm.usage.counter51.JsonProperties.BEGIN_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.CREATED;
import static org.olf.erm.usage.counter51.JsonProperties.END_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ATTRIBUTES;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ID;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ITEMS;
import static org.olf.erm.usage.counter51.ReportMerger.MSG_PROPERTIES_DO_NOT_MATCH;
import static org.olf.erm.usage.counter51.ReportMerger.MergerException.MSG_ERROR_MERGING_REPORT;
import static org.olf.erm.usage.counter51.ReportSplitter.SplitterException.MSG_ERROR_SPLITTING_REPORT;
import static org.olf.erm.usage.counter51.ReportType.DR;
import static org.olf.erm.usage.counter51.TestUtil.assertThatReportLinesAreEqualIgnoringOrder;
import static org.olf.erm.usage.counter51.TestUtil.getLinesFromString;
import static org.olf.erm.usage.counter51.TestUtil.getObjectMapper;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsLines;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsObjectNode;
import static org.olf.erm.usage.counter51.TestUtil.removeBOMAndTrailingDelimiters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.olf.erm.usage.counter51.ReportSplitter.SplitterException;

class Counter51UtilsTest {

  private final ObjectMapper objectMapper = getObjectMapper();

  @ParameterizedTest
  @EnumSource(ReportType.class)
  void testSplitReportAndMergeReports(ReportType reportType) throws IOException {
    ObjectNode expectedReport = readFileAsObjectNode(getSampleReportPath(reportType).toFile());
    ObjectNode expectedReportOriginal = expectedReport.deepCopy();

    List<ObjectNode> splitReports = splitReport(expectedReport);
    assertThat(splitReports)
        .hasSize(12)
        .allSatisfy(this::assertThatEachPerformanceMetricHasSingleMonthData);
    List<ObjectNode> splitReportsOriginal =
        splitReports.stream().map(ObjectNode::deepCopy).toList();

    // test with multi-month reports
    ObjectNode m1 = mergeReports(splitReports.subList(0, 6));
    ObjectNode m2 = mergeReports(splitReports.subList(5, 12));
    ObjectNode mergedReport = mergeReports(List.of(m2, m1));

    assertThatJson(mergedReport)
        .when(IGNORING_ARRAY_ORDER)
        .whenIgnoringPaths(REPORT_HEADER + "." + CREATED)
        .isEqualTo(expectedReport);

    // test that input is not modified
    assertThat(expectedReport).isEqualTo(expectedReportOriginal);
    assertThat(splitReports).isEqualTo(splitReportsOriginal);
  }

  @Test
  void testMergeReportsWithDifferentProperties() {
    ObjectNode report1 = objectMapper.createObjectNode();
    report1
        .withObject("/" + REPORT_HEADER + "/" + REPORT_FILTERS)
        .put(BEGIN_DATE, "2022-01-01")
        .put(END_DATE, "2022-01-31");
    report1.withObject(REPORT_HEADER).put(REPORT_ID, "TR");

    ObjectNode report2 = objectMapper.createObjectNode();
    report2
        .withObject("/" + REPORT_HEADER + "/" + REPORT_FILTERS)
        .put(BEGIN_DATE, "2022-02-01")
        .put(END_DATE, "2022-02-28");
    report2.withObject(REPORT_HEADER).put(REPORT_ID, "IR");

    assertThatThrownBy(() -> mergeReports(Arrays.asList(report1, report2)))
        .hasMessage(MSG_ERROR_MERGING_REPORT + MSG_PROPERTIES_DO_NOT_MATCH);
  }

  @ParameterizedTest
  @EnumSource(
      value = ReportType.class,
      names = {"TR", "DR", "IR", "PR"})
  void testMergeReportsWithReorderedReportAttributes(ReportType reportType) throws IOException {
    ObjectNode report = readFileAsObjectNode(getSampleReportPath(reportType).toFile());
    List<ObjectNode> splitReports = splitReport(report);

    ArrayNode attributesToShow =
        (ArrayNode)
            splitReports
                .get(0)
                .path(REPORT_HEADER)
                .path(REPORT_ATTRIBUTES)
                .path(ATTRIBUTES_TO_SHOW);

    List<JsonNode> reversedElements =
        StreamSupport.stream(attributesToShow.spliterator(), false).toList().reversed();
    attributesToShow.removeAll().addAll(reversedElements);

    ObjectNode mergedReport = mergeReports(splitReports);
    assertThatJson(mergedReport)
        .when(IGNORING_ARRAY_ORDER)
        .whenIgnoringPaths(REPORT_HEADER + "." + CREATED)
        .isEqualTo(report);
  }

  @Test
  void testSplitReportWithInvalidReport() {
    ObjectNode report = objectMapper.createObjectNode();

    assertThatThrownBy(() -> splitReport(report))
        .hasMessageStartingWith(MSG_ERROR_SPLITTING_REPORT);
  }

  @ParameterizedTest
  @MethodSource
  void testSplitReportWithMalformedReportItems(String reportItems, String expectedMessage) {
    ObjectNode report = createReportWithReportItems(reportItems);

    assertThatThrownBy(() -> splitReport(report))
        .isInstanceOf(SplitterException.class)
        .hasMessage(MSG_ERROR_SPLITTING_REPORT + expectedMessage);
  }

  static Stream<Arguments> testSplitReportWithMalformedReportItems() {
    return Stream.of(
        arguments("null", "Expected Report_Items to be an array but was NULL"),
        arguments("'garbage'", "Expected Report_Items to be an array but was STRING"),
        arguments("{'Title': 'garbage'}", "Expected Report_Items to be an array but was OBJECT"),
        arguments("[null]", "Expected an entry of Report_Items to be an object but was NULL"),
        arguments(
            "['garbage']", "Expected an entry of Report_Items to be an object but was STRING"),
        arguments(
            "[{'Attribute_Performance': null}]",
            "Expected Attribute_Performance to be an array but was NULL"),
        arguments(
            "[{'Attribute_Performance': 'garbage'}]",
            "Expected Attribute_Performance to be an array but was STRING"),
        arguments(
            "[{'Attribute_Performance': ['garbage']}]",
            "Expected an entry of Attribute_Performance to be an object but was STRING"),
        arguments(
            "[{'Attribute_Performance': [{'Performance': 'garbage'}]}]",
            "Expected Performance to be an object but was STRING"),
        arguments(
            "[{'Attribute_Performance': [{'Performance': []}]}]",
            "Expected Performance to be an object but was ARRAY"),
        arguments(
            "[{'Attribute_Performance': [{'Performance': {'Total_Item_Requests': 'garbage'}}]}]",
            "Expected Performance.Total_Item_Requests to be an object but was STRING"),
        arguments("[{'Items': null}]", "Expected Items to be an array but was NULL"),
        arguments("[{'Items': 'garbage'}]", "Expected Items to be an array but was STRING"),
        arguments(
            "[{'Items': ['garbage']}]",
            "Expected an entry of Items to be an object but was STRING"),
        arguments(
            "[{'Items': [{'Attribute_Performance': 'garbage'}]}]",
            "Expected Attribute_Performance to be an array but was STRING"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "[]",
        "[{'Title': 'Without usage'}]",
        "[{'Attribute_Performance': []}]",
        "[{'Attribute_Performance': [{'Performance': null}]}]",
        "[{'Attribute_Performance': [{'Performance': {}}]}]",
        "[{'Attribute_Performance': [{'Performance': {'Total_Item_Requests': null}}]}]",
        "[{'Items': []}]"
      })
  void testSplitReportWithoutUsage(String reportItems) {
    ObjectNode report = createReportWithReportItems(reportItems);

    assertThat(splitReport(report)).hasSize(2);
  }

  @Test
  void testSplitReportWithoutReportItems() {
    ObjectNode report = createReportWithReportItems(null);

    assertThat(splitReport(report))
        .hasSize(2)
        .allSatisfy(
            monthlyReport -> assertThatJson(monthlyReport.get(REPORT_ITEMS)).isEqualTo("[]"));
  }

  private ObjectNode createReportWithReportItems(String reportItems) {
    String reportItemsProperty =
        (reportItems == null) ? "" : ", '%s': %s".formatted(REPORT_ITEMS, reportItems);
    String report =
        "{'%s': {'%s': 'TR', '%s': {'%s': '2022-01-01', '%s': '2022-02-28'}}%s}"
            .formatted(
                REPORT_HEADER,
                REPORT_ID,
                REPORT_FILTERS,
                BEGIN_DATE,
                END_DATE,
                reportItemsProperty);
    try {
      return (ObjectNode) objectMapper.readTree(report.replace('\'', '"'));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private ObjectNode createReportItem(String title, Map<String, Map<String, Integer>> performance) {
    ObjectNode reportItem = objectMapper.createObjectNode().put("Title", title);
    ObjectNode target =
        reportItem.putArray("Attribute_Performance").addObject().putObject(PERFORMANCE);
    performance.forEach(
        (metricType, counts) -> {
          ObjectNode metric = target.putObject(metricType);
          counts.forEach(metric::put);
        });
    return reportItem;
  }

  /**
   * The sample reports all carry every metric in every month, so they cannot show that report items
   * and metrics are dropped from the months they hold no usage for.
   */
  @Test
  void testSplitReportWithUsageInSomeMonthsOnly() {
    ObjectNode report = objectMapper.createObjectNode();
    report
        .putObject(REPORT_HEADER)
        .put(REPORT_ID, "TR")
        .putObject(REPORT_FILTERS)
        .put(BEGIN_DATE, "2022-01-01")
        .put(END_DATE, "2022-02-28");
    ArrayNode reportItems = report.putArray("Report_Items");
    reportItems.add(
        createReportItem("January only", Map.of("Total_Item_Requests", Map.of("2022-01", 1))));
    reportItems.add(
        createReportItem("February only", Map.of("Total_Item_Requests", Map.of("2022-02", 2))));
    reportItems.add(
        createReportItem(
            "Both months",
            Map.of(
                "Total_Item_Requests", Map.of("2022-01", 3, "2022-02", 4),
                "Unique_Item_Requests", Map.of("2022-01", 5))));

    List<ObjectNode> splitReports = splitReport(report);

    assertThat(splitReports).hasSize(2);
    assertThat(titlesOf(splitReports.get(0))).containsExactly("January only", "Both months");
    assertThat(titlesOf(splitReports.get(1))).containsExactly("February only", "Both months");
    // Unique_Item_Requests has no February usage and must not survive into the February report
    assertThatJson(performanceOf(splitReports.get(0), 1))
        .isEqualTo(
            "{\"Total_Item_Requests\":{\"2022-01\":3},\"Unique_Item_Requests\":{\"2022-01\":5}}");
    assertThatJson(performanceOf(splitReports.get(1), 1))
        .isEqualTo("{\"Total_Item_Requests\":{\"2022-02\":4}}");
  }

  /**
   * An attribute performance that holds no usage for the month is kept, with an empty Performance.
   * Pinned here because the sample reports cannot show it: they carry every metric in every month.
   */
  @Test
  void testSplitReportKeepsAttributePerformancesWithoutUsageOfMonth() {
    ObjectNode report = objectMapper.createObjectNode();
    report
        .putObject(REPORT_HEADER)
        .put(REPORT_ID, "TR")
        .putObject(REPORT_FILTERS)
        .put(BEGIN_DATE, "2022-01-01")
        .put(END_DATE, "2022-02-28");
    ObjectNode reportItem = report.putArray("Report_Items").addObject();
    reportItem.put("Title", "Two attribute performances");
    ArrayNode attributePerformances = reportItem.putArray("Attribute_Performance");
    attributePerformances
        .addObject()
        .put("Data_Type", "Journal")
        .putObject(PERFORMANCE)
        .putObject("Total_Item_Requests")
        .put("2022-01", 1);
    attributePerformances
        .addObject()
        .put("Data_Type", "Book")
        .putObject(PERFORMANCE)
        .putObject("Total_Item_Requests")
        .put("2022-02", 2);

    List<ObjectNode> splitReports = splitReport(report);

    assertThatJson(splitReports.get(0).at("/Report_Items/0/Attribute_Performance"))
        .isEqualTo(
            "[{'Data_Type':'Journal','Performance':{'Total_Item_Requests':{'2022-01':1}}},"
                + "{'Data_Type':'Book','Performance':{}}]");
    assertThatJson(splitReports.get(1).at("/Report_Items/0/Attribute_Performance"))
        .isEqualTo(
            "[{'Data_Type':'Journal','Performance':{}},"
                + "{'Data_Type':'Book','Performance':{'Total_Item_Requests':{'2022-02':2}}}]");
  }

  private List<String> titlesOf(ObjectNode report) {
    return StreamSupport.stream(report.withArray("Report_Items").spliterator(), false)
        .map(item -> item.get("Title").asText())
        .toList();
  }

  private JsonNode performanceOf(ObjectNode report, int itemIndex) {
    return report
        .withArray("Report_Items")
        .get(itemIndex)
        .at("/Attribute_Performance/0/Performance");
  }

  @Test
  void testWriteReportAsCsv() throws IOException {
    Path sampleDRJsonPath = getSampleReportPath(DR);
    Path sampleDRTsvPath = getSampleReportPath(DR, "tsv");

    ObjectNode report = readFileAsObjectNode(sampleDRJsonPath.toFile());

    StringWriter stringWriter = new StringWriter();
    writeReportAsCsv(report, stringWriter);
    List<String> actualLines = getLinesFromString(stringWriter.toString(), "\r\n");

    // replace tabs with commas for comparison
    List<String> tsv = removeBOMAndTrailingDelimiters(readFileAsLines(sampleDRTsvPath), "\t");
    List<String> expectedLines = tsv.stream().map(line -> line.replaceAll("\t", ",")).toList();

    assertThatReportLinesAreEqualIgnoringOrder(actualLines, expectedLines);
  }

  private void assertThatEachPerformanceMetricHasSingleMonthData(JsonNode report) {
    report
        .findValues(PERFORMANCE)
        .forEach(node -> node.fields().forEachRemaining(e -> assertThat(e.getValue()).hasSize(1)));
  }
}
