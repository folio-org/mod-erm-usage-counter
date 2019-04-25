package org.olf.erm.usage.counter41.csv.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.bind.JAXB;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.niso.schemas.counter.Report;
import org.niso.schemas.sushi.counter.CounterReportResponse;
import org.olf.erm.usage.counter41.csv.CSVMapper;

@RunWith(Parameterized.class)
public class CSVMapperTest {

  private String input;
  private String expected;

  @Parameters(name = "{0}")
  public static Collection params() {
    return Arrays.asList("JR1", "DB1", "BR2");
  }

  public CSVMapperTest(String reportName) {
    this.input = "reports/" + reportName + ".xml";
    this.expected = "reports/" + reportName + ".csv";
  }

  @Test
  public void testToCSV() throws URISyntaxException, IOException {
    File file = new File(Resources.getResource(input).toURI());
    String expectedString =
        new String(Resources.toByteArray(Resources.getResource(expected)))
            .replace("$$$date_run$$$", LocalDate.now().toString());
    Report report =
        JAXB.unmarshal(file, CounterReportResponse.class).getReport().getReport().get(0);

    String result = CSVMapper.toCSV(report);
    assertThat(result).isEqualToIgnoringNewLines(expectedString);
  }

  @Test
  public void testNoTitle() {
    Report report = new Report();
    report.setVersion("4");
    report.setTitle("Report XYZ");
    String result = CSVMapper.toCSV(report);
    assertThat(result).isNull();
  }
}
