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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olf.erm.usage.counter50.Counter5Utils;

@RunWith(Parameterized.class)
public class MapperTest {

  private final String input;
  private final String expected;

  public MapperTest(String reportName) {
    this.input = "reports/" + reportName + ".json";
    this.expected = "reports/" + reportName + ".csv";
  }

  @Parameters(name = "{0}")
  public static Collection params() {
    return Arrays.asList("IR", "PR", "TR");
  }

  @Test
  public void testToCSV() throws IOException, MapperException {
    URL url = Resources.getResource(input);
    String jsonString = Resources.toString(url, StandardCharsets.UTF_8);
    String expectedString =
        new String(Resources.toByteArray(Resources.getResource(expected)))
            .replace("$$$date_run$$$", LocalDate.now().toString());
    Object report = Counter5Utils.fromJSON(jsonString);
    String result = MapperFactory.createCSVMapper(report).toCSV();
    assertThat(result).isEqualToIgnoringNewLines(expectedString);
  }
}
