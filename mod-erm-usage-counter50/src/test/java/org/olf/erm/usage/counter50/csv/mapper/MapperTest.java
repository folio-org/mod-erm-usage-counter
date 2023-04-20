package org.olf.erm.usage.counter50.csv.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter50.TestUtil.sort;

import com.google.common.collect.Streams;
import com.google.common.io.Resources;
import io.vertx.core.json.Json;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
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
      URL url = Resources.getResource(input);
      String jsonString = Resources.toString(url, StandardCharsets.UTF_8);
      Object report = Counter5Utils.fromJSON(jsonString);
      String result = MapperFactory.createReportToCsvMapper(report).toCSV();
      String expectedString =
          new String(Resources.toByteArray(Resources.getResource(expected)))
              .replace("$$$date_run$$$", LocalDate.now().toString());
      assertThat(result).isEqualToIgnoringNewLines(expectedString);
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
          Resources.toString(Resources.getResource(input + ".csv"), StandardCharsets.UTF_8)
              .replace("$$$date_run$$$", LocalDate.now().toString());
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
      this.input = "reports/" + reportName + ".csv";
    }

    @Parameters(name = "{0}")
    public static Collection<String> params() {
      return Arrays.asList("TR_1", "TR_merged", "IR_1", "PR_1", "PR_merged", "DR_1");
    }

    @Test
    public void testToReports() throws IOException, MapperException {
      String csvString =
          Resources.toString(Resources.getResource(input), StandardCharsets.UTF_8)
              .replace("$$$date_run$$$", LocalDate.now().toString());
      CsvToReportMapper mapper = MapperFactory.createCsvToReportMapper(csvString);
      Object resultReport = mapper.toReport();
      String resultCSV = MapperFactory.createReportToCsvMapper(resultReport).toCSV();

      StringReader stringReaderExpected = new StringReader(csvString);
      List<String> linesExpected = IOUtils.readLines(stringReaderExpected);

      StringReader stringReaderActual = new StringReader(resultCSV);
      List<String> linesActual = IOUtils.readLines(stringReaderActual);

      for (String expected : linesExpected) {
        if (expected.startsWith("Metric_Types")) {
          assertThatMetricTypesAreEqual(expected, linesActual);
        } else {
          assertThat(linesActual).contains(expected);
        }
      }
    }

    private void assertThatMetricTypesAreEqual(String expectedLine, List<String> actualReport) {
      String actualLine =
          actualReport.stream().filter(s -> s.startsWith("Metric_Types")).findFirst().orElse("");
      String[] split = expectedLine.replace("Metric_Types,", "").split(";");
      for (String s : split) {
        assertThat(actualLine).contains(s.trim());
      }
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
