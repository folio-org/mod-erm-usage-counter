package org.olf.erm.usage.counter41.csv.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.bind.JAXB;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.Report;
import org.niso.schemas.sushi.counter.CounterReportResponse;
import org.olf.erm.usage.counter41.csv.mapper.csv2report.CsvToReportMapperFactory;

@Ignore
@RunWith(Parameterized.class)
public class AbstractCsvToReportMapperTest {

  private final String input;
  private final String expected;

  @Parameters(name = "{0}")
  public static Collection params() {
    return Arrays.asList("JR1");
  }

  public AbstractCsvToReportMapperTest(String reportName) {
    this.input = "reports/" + reportName + ".csv";
    this.expected = "reports/" + reportName + ".xml";
  }

  private void removeAttributes(Report report) {
    report.setVendor(null);
    report.setCreated(null);
    report.setID(null);
    report.getCustomer().get(0).getReportItems().stream()
        .flatMap(ri -> ri.getItemPerformance().stream())
        .map(Metric::getInstance)
        .forEach(
            list ->
                list.removeIf(
                    pc ->
                        pc.getMetricType().equals(MetricType.FT_HTML)
                            || pc.getMetricType().equals(MetricType.FT_PDF)));
  }

  @Test
  public void testFromCSV() throws URISyntaxException, IOException, MapperException {
    String csvString = Resources.toString(Resources.getResource(input), StandardCharsets.UTF_8);

    File file = new File(Resources.getResource(expected).toURI());
    Report expectedReport =
        JAXB.unmarshal(file, CounterReportResponse.class).getReport().getReport().get(0);
    removeAttributes(expectedReport);

    Report parsedReport = CsvToReportMapperFactory.createCsvToReportMapper(csvString).toReport();
    assertThat(parsedReport).isEqualToComparingFieldByFieldRecursively(expectedReport);
  }
}