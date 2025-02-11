package org.olf.erm.usage.counter51;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter51.Counter51Utils.createDefaultObjectMapper;
import static org.olf.erm.usage.counter51.Counter51Utils.mergeReports;
import static org.olf.erm.usage.counter51.Counter51Utils.splitReport;
import static org.olf.erm.usage.counter51.JsonProperties.BEGIN_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.CREATED;
import static org.olf.erm.usage.counter51.JsonProperties.END_DATE;
import static org.olf.erm.usage.counter51.JsonProperties.PERFORMANCE;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_FILTERS;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_HEADER;
import static org.olf.erm.usage.counter51.JsonProperties.REPORT_ID;
import static org.olf.erm.usage.counter51.ReportMerger.MSG_PROPERTIES_DO_NOT_MATCH;
import static org.olf.erm.usage.counter51.ReportMerger.MSG_REPORT_SPANS_MULTIPLE_MONTHS;
import static org.olf.erm.usage.counter51.ReportMerger.MergerException.MSG_ERROR_MERGING_REPORT;
import static org.olf.erm.usage.counter51.ReportSplitter.SplitterException.MSG_ERROR_SPLITTING_REPORT;
import static org.olf.erm.usage.counter51.TestUtil.getObjectMapper;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsObjectNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class Counter51UtilsTest {

  private final ObjectMapper objectMapper = getObjectMapper();

  @Test
  void testCreateDefaultObjectMapper() {
    ObjectMapper expected = Mockito.mock(ObjectMapper.class);
    try (MockedStatic<ObjectMapperFactory> mockedStatic =
        Mockito.mockStatic(ObjectMapperFactory.class)) {
      mockedStatic.when(ObjectMapperFactory::createDefault).thenReturn(expected);
      assertThat(createDefaultObjectMapper()).isEqualTo(expected);
    }
  }

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

    ObjectNode mergedReport = mergeReports(splitReports);

    assertThatJson(mergedReport)
        .when(IGNORING_ARRAY_ORDER)
        .whenIgnoringPaths(REPORT_HEADER + "." + CREATED)
        .isEqualTo(expectedReport);

    // test that input is not modified
    assertThat(expectedReport).isEqualTo(expectedReportOriginal);
    assertThat(splitReports).isEqualTo(splitReportsOriginal);
  }

  @Test
  void testMergeReportsThatSpanMultipleMonths() {
    ObjectNode report1 = objectMapper.createObjectNode();
    report1
        .withObject("/" + REPORT_HEADER + "/" + REPORT_FILTERS)
        .put(BEGIN_DATE, "2022-01-01")
        .put(END_DATE, "2022-03-31");

    ObjectNode report2 = objectMapper.createObjectNode();
    report2
        .withObject("/" + REPORT_HEADER + "/" + REPORT_FILTERS)
        .put(BEGIN_DATE, "2022-04-01")
        .put(END_DATE, "2022-04-30");

    assertThatThrownBy(() -> mergeReports(Arrays.asList(report1, report2)))
        .hasMessageContaining(MSG_ERROR_MERGING_REPORT + MSG_REPORT_SPANS_MULTIPLE_MONTHS);
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

  @Test
  void testSplitReportWithInvalidReport() {
    ObjectNode report = objectMapper.createObjectNode();

    assertThatThrownBy(() -> splitReport(report))
        .hasMessageStartingWith(MSG_ERROR_SPLITTING_REPORT);
  }

  private void assertThatEachPerformanceMetricHasSingleMonthData(JsonNode report) {
    report
        .findValues(PERFORMANCE)
        .forEach(node -> node.fields().forEachRemaining(e -> assertThat(e.getValue()).hasSize(1)));
  }
}
