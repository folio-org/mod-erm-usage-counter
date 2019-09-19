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
import org.olf.erm.usage.counter41.csv.mapper.report2csv.ReportToCsvMapperFactory;

@RunWith(Parameterized.class)
public class ReportToCsvMapperTest {

  private final String input;
  private final String expected;

  @Parameters(name = "{0}")
  public static Collection params() {
    return Arrays.asList("JR1", "DB1", "BR2", "PR1", "BR1");
  }

  public ReportToCsvMapperTest(String reportName) {
    this.input = "reports/" + reportName + ".xml";
    this.expected = "reports/" + reportName + ".csv";
  }

  @Test
  public void testToCSV() throws URISyntaxException, IOException, MapperException {
    File file = new File(Resources.getResource(input).toURI());
    String expectedString =
        new String(Resources.toByteArray(Resources.getResource(expected)))
            .replace("$$$date_run$$$", LocalDate.now().toString());
    Report report =
        JAXB.unmarshal(file, CounterReportResponse.class).getReport().getReport().get(0);

    String result = ReportToCsvMapperFactory.createCSVMapper(report).toCSV();
    assertThat(result).isEqualToIgnoringNewLines(expectedString);
  }

  @Test(expected = MapperException.class)
  public void testNoTitle() throws MapperException {
    Report report = new Report();
    report.setVersion("4");
    report.setTitle("Report XYZ");
    ReportToCsvMapperFactory.createCSVMapper(report).toCSV();
  }
}
