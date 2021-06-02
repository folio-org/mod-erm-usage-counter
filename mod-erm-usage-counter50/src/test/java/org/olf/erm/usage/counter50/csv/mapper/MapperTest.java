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

@RunWith(Enclosed.class)
public class MapperTest {

  @RunWith(Parameterized.class)
  public static class TestReportToCsv {

    private final String input;
    private final String expected;

    public TestReportToCsv(String reportName) {
      this.input = "reports/" + reportName + ".json";
      this.expected = "reports/" + reportName + ".csv";
    }

    @Parameters(name = "{0}")
    public static Collection params() {
      return Arrays.asList("DR_1", "IR_1", "PR_1", "TR_1", "PR_merged");
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

    public TestCsvToReport(String reportName) {
      this.input = "reports/" + reportName + ".csv";
    }

    @Parameters(name = "{0}")
    public static Collection params() {
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
}
