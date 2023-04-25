package org.olf.erm.usage.counter50.csv.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter50.TestUtil.sort;

import com.google.common.collect.Streams;
import com.google.common.io.Resources;
import io.vertx.core.json.Json;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.CsvToReportMapper;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.TRCsvToReport;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.SUSHIErrorModel;

@RunWith(Enclosed.class)
public class MapperTest {

  private static Class<?> getCounterClass(Object o) {
    if (o instanceof COUNTERTitleReport) {
      return COUNTERTitleReport.class;
    }
    if (o instanceof COUNTERDatabaseReport) {
      return COUNTERDatabaseReport.class;
    }
    if (o instanceof COUNTERPlatformReport) {
      return COUNTERPlatformReport.class;
    }
    if (o instanceof COUNTERItemReport) {
      return COUNTERItemReport.class;
    }
    throw new IllegalArgumentException();
  }

  @RunWith(Parameterized.class)
  public static class TestToAndFromCSV<T> {

    private final String input;
    private final Class<T> clazz;

    public TestToAndFromCSV(String reportName, Class<T> clazz) {
      this.input = "reports/full/" + reportName + ".json";
      this.clazz = clazz;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> params() {
      return List.of(
          new Object[] {"tr", COUNTERTitleReport.class},
          new Object[] {"pr", COUNTERPlatformReport.class},
          new Object[] {"ir", COUNTERItemReport.class},
          new Object[] {"dr", COUNTERDatabaseReport.class});
    }

    @Test
    public void testToAndFromCSV() throws IOException, Counter5UtilsException, MapperException {
      String expectedReportStr =
          Resources.toString(Resources.getResource(input), StandardCharsets.UTF_8);
      T expectedReport = clazz.cast(Counter5Utils.fromJSON(expectedReportStr));

      String csv = Counter5Utils.toCSV(expectedReport);
      T actualReport = clazz.cast(Counter5Utils.fromCSV(csv));

      sort(actualReport);
      sort(expectedReport);

      assertThat(actualReport)
          .usingRecursiveComparison()
          .withEqualsForFields( // remove linebreaks in SUSHIErrorModel.data
              (List<SUSHIErrorModel> a, List<SUSHIErrorModel> e) -> {
                if ((a == null || a.isEmpty()) && (e == null || e.isEmpty())) {
                  return true;
                }
                e.forEach(
                    em -> {
                      if (em.getData() != null) {
                        em.setData(em.getData().replaceAll("\\R", " "));
                      }
                    });
                return a != null && a.equals(e);
              },
              "reportHeader.exceptions")
          .withEqualsForFields(
              (List<?> a, List<?> e) ->
                  ((a == null || a.isEmpty()) && (e == null || e.isEmpty()))
                      || (a != null && a.equals(e)),
              "reportHeader.reportAttributes",
              "reportItems.itemID")
          .ignoringFields("reportHeader.created", "reportHeader.customerID")
          .isEqualTo(expectedReport);
    }
  }

  @RunWith(Parameterized.class)
  public static class TestReportToCsv {

    private final String input;
    private final String expected;

    public TestReportToCsv(String reportName) {
      this.input = "reports/" + reportName + ".json";
      this.expected = "reports/" + reportName + ".csv";
    }

    @Parameters(name = "{0}")
    public static Collection<String> params() {
      return Arrays.asList(
          "DR_1",
          "DRD1_merged",
          "IR_1",
          "PR_1",
          "TR_1",
          "PR_merged",
          "TRB1_merged",
          "TRB3_merged",
          "TRJ1_merged",
          "TRJ3_merged",
          "TRJ4_merged");
    }

    @Test
    public void testToCSV() throws IOException, MapperException, Counter5UtilsException {
      String jsonString = Resources.toString(Resources.getResource(input), StandardCharsets.UTF_8);
      Object report = Counter5Utils.fromJSON(jsonString);
      String actualString = MapperFactory.createReportToCsvMapper(report).toCSV();
      String expectedString =
          Resources.toString(Resources.getResource(expected), StandardCharsets.UTF_8);

      List<String> actualLines = actualString.lines().collect(Collectors.toList());
      List<String> expectedLines = expectedString.lines().collect(Collectors.toList());
      String actualCreated = actualLines.remove(10);
      expectedLines.remove(10);

      assertThat(actualLines).containsExactlyElementsOf(expectedLines);
      assertThat(actualCreated).matches("Created,\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
    }
  }

  @RunWith(Parameterized.class)
  public static class TestPcSampleReports {
    private final String input;

    public TestPcSampleReports(String reportName) {
      this.input = "reports/projectcounter-samples/Sample-" + reportName;
    }

    @Parameters(name = "{0}")
    public static Collection<String> params() {
      return Arrays.asList("TR", "DR", "PR", "IR");
    }

    @Test
    public void testCsvToReport() throws MapperException, IOException {
      String csvString =
          Resources.toString(Resources.getResource(input + ".csv"), StandardCharsets.UTF_8);
      CsvToReportMapper mapper = MapperFactory.createCsvToReportMapper(csvString);
      Object actualReport = mapper.toReport();

      String jsonString =
          Resources.toString(Resources.getResource(input + ".json"), StandardCharsets.UTF_8);
      Object expectedReport =
          Json.decodeValue(jsonString, MapperTest.getCounterClass(actualReport));

      assertThat(actualReport).extracting("reportHeader.customerID").isNull();
      assertThat(actualReport)
          .usingRecursiveComparison()
          .ignoringCollectionOrder()
          .ignoringFields("reportHeader.customerID")
          .isEqualTo(expectedReport);
    }

    @Test
    public void testReportToCsv() throws IOException, MapperException, Counter5UtilsException {
      URL url = Resources.getResource(input + ".json");
      String jsonString = Resources.toString(url, StandardCharsets.UTF_8);
      Object expectedReport = Counter5Utils.fromJSON(jsonString);
      String result = MapperFactory.createReportToCsvMapper(expectedReport).toCSV();
      String expectedString =
          new String(Resources.toByteArray(Resources.getResource(input + ".csv")));

      // test header, ignore trailing commas and Created date
      Streams.zip(result.lines().limit(14), expectedString.lines().limit(14), List::of)
          .forEach(
              l -> {
                String left = StringUtils.removeEnd(l.get(0), ",");
                String right = StringUtils.removeEnd(l.get(1), ",");
                if (left.startsWith("Created")) {
                  return;
                }
                assertThat(left).isEqualTo(right);
              });

      assertThat(result.lines().skip(14).collect(Collectors.toList()))
          .hasSameElementsAs(expectedString.lines().skip(14).collect(Collectors.toList()));
    }
  }

  @RunWith(Parameterized.class)
  public static class TestCsvToReport {

    private final String input;

    public TestCsvToReport(String reportName) {
      this.input = "reports/" + reportName;
    }

    @Parameters(name = "{0}")
    public static Collection<String> params() {
      return Arrays.asList("TR_1", "TR_merged", "IR_1", "PR_1", "PR_merged", "DR_1");
    }

    @Test
    public void testToReports() throws IOException, MapperException {
      String csvString =
          Resources.toString(Resources.getResource(input + ".csv"), StandardCharsets.UTF_8);
      Object actual = MapperFactory.createCsvToReportMapper(csvString).toReport();

      String expectedReportStr =
          Resources.toString(Resources.getResource(input + ".json"), StandardCharsets.UTF_8);
      Object expected = Json.decodeValue(expectedReportStr, getCounterClass(actual));

      assertThat(actual)
          .usingRecursiveComparison()
          .ignoringCollectionOrder()
          .withEqualsForFields(
              (BiPredicate<List<?>, List<?>>)
                  (l1, l2) -> {
                    if (CollectionUtils.isEmpty(l1) && CollectionUtils.isEmpty(l2)) {
                      return true;
                    } else {
                      return l1.equals(l2);
                    }
                  },
              "reportHeader.exceptions")
          .ignoringFields("reportHeader.created", "reportHeader.customerID")
          .isEqualTo(expected);
    }
  }

  public static class MapperFactoryTest {

    @Test
    public void testCsvWithQuotes() throws IOException, MapperException {
      String inputStr =
          Resources.toString(
              Resources.getResource("reports/TR_withquotes.csv"), StandardCharsets.UTF_8);
      CsvToReportMapper result = MapperFactory.createCsvToReportMapper(inputStr);
      assertThat(result).isInstanceOf(TRCsvToReport.class);
    }

    @Test
    public void testCsvWithNoContent() {
      String inputStr = "";
      assertThatThrownBy(() -> MapperFactory.createCsvToReportMapper(inputStr))
          .isInstanceOf(MapperException.class)
          .hasMessageContaining("Cant read first line");
    }

    @Test
    public void testCsvWithUnknownReport() throws IOException {
      String inputStr =
          Resources.toString(
              Resources.getResource("reports/TRB1_merged.csv"), StandardCharsets.UTF_8);
      assertThatThrownBy(() -> MapperFactory.createCsvToReportMapper(inputStr))
          .isInstanceOf(MapperException.class)
          .hasMessageContaining("unknown name");
    }
  }
}
