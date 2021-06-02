package org.olf.erm.usage.counter50.csv.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.CsvToReportMapper;
import org.openapitools.client.model.COUNTERPlatformReport;

@RunWith(Enclosed.class)
public class MapperTest {

  @RunWith(Parameterized.class)
  public static class SingleMonthTest {

    private final String input;
    private final String expected;

    public SingleMonthTest(String reportName) {
      this.input = "reports/" + reportName + ".json";
      this.expected = "reports/" + reportName + ".csv";
    }

    @Parameters(name = "{0}")
    public static Collection params() {
      return Arrays.asList("DR_1", "IR_1", "PR_1", "TR_1");
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
  public static class TestCsvToReport {

    private final String input;
    private final String expected;

    public TestCsvToReport(String reportName) {
      this.input = "reports/" + reportName + ".json";
      this.expected = "reports/" + reportName + ".csv";
    }

    @Parameters(name = "{0}")
    public static Collection params() {
      return Arrays.asList("TR_1", "TR_merged", "IR_1", "PR_1", "PR_merged", "DR_1");
    }

    @Test
    public void testToReports() throws IOException, MapperException {

      String csvString =
          Resources.toString(Resources.getResource(expected), StandardCharsets.UTF_8);
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

  public static class MultiMonthTest {

    @Test
    public void testToCSV() throws IOException, MapperException, Counter5UtilsException {
      URL url1 = Resources.getResource("reports/PR_1.json");
      String jsonString1 = Resources.toString(url1, StandardCharsets.UTF_8);
      COUNTERPlatformReport report1 = (COUNTERPlatformReport) Counter5Utils.fromJSON(jsonString1);
      URL url2 = Resources.getResource("reports/PR_2.json");
      String jsonString2 = Resources.toString(url2, StandardCharsets.UTF_8);
      COUNTERPlatformReport report2 = (COUNTERPlatformReport) Counter5Utils.fromJSON(jsonString2);
      URL url3 = Resources.getResource("reports/PR_3.json");
      String jsonString3 = Resources.toString(url3, StandardCharsets.UTF_8);
      COUNTERPlatformReport report3 = (COUNTERPlatformReport) Counter5Utils.fromJSON(jsonString3);
      COUNTERPlatformReport mergedPlatformReport =
          Counter5Utils.merge(Arrays.asList(report1, report2, report3));
      String result = MapperFactory.createReportToCsvMapper(mergedPlatformReport).toCSV();

      String expectedString =
          new String(Resources.toByteArray(Resources.getResource("reports/PR_merged.csv")))
              .replace("$$$date_run$$$", LocalDate.now().toString());
      assertThat(result).isEqualToIgnoringNewLines(expectedString);
    }
  }
}
