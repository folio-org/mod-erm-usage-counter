package org.olf.erm.usage.counter50.csv.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
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
    public void testToCSV() throws IOException, MapperException {
      URL url = Resources.getResource(input);
      String jsonString = Resources.toString(url, StandardCharsets.UTF_8);
      Object report = Counter5Utils.fromJSON(jsonString);
      String result = MapperFactory.createCSVMapper(report).toCSV();
      String expectedString =
          new String(Resources.toByteArray(Resources.getResource(expected)))
              .replace("$$$date_run$$$", LocalDate.now().toString());
      assertThat(result).isEqualToIgnoringNewLines(expectedString);
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
      COUNTERPlatformReport mergedPlatformReport = Counter5Utils.merge(Arrays.asList(report1, report2, report3));
      String result = MapperFactory.createCSVMapper(mergedPlatformReport).toCSV();

      String expectedString =
          new String(Resources.toByteArray(Resources.getResource("reports/PR_merged.csv")))
              .replace("$$$date_run$$$", LocalDate.now().toString());
      assertThat(result).isEqualToIgnoringNewLines(expectedString);
    }
  }
}

