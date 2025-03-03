package org.olf.erm.usage.counter41.csv.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import jakarta.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.niso.schemas.counter.IdentifierType;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.MetricType;
import org.niso.schemas.counter.Report;
import org.niso.schemas.sushi.counter.CounterReportResponse;

@RunWith(Parameterized.class)
public class MapperTest {

  private final String input;
  private final String expected;
  private final String reportName;

  @Parameters(name = "{0}")
  public static Collection params() {
    return Arrays.asList("JR1", "DB1", "BR2", "PR1", "BR1");
  }

  public MapperTest(String reportName) {
    this.reportName = reportName;
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

    String result = MapperFactory.createCSVMapper(report).toCSV();
    assertThat(result).isEqualToIgnoringNewLines(expectedString);
  }

  /**
   * removes attributes from report that cannot be mapped from csv
   *
   * @param report
   */
  private void removeAttributes(Report report) {
    report.getCustomer().get(0).getReportItems().stream()
        .peek(
            ri -> {
              if (reportName.equals("PR1")) ri.setItemName(null);
              if (reportName.equals("DB1")) ri.setItemIdentifier(null);
              if (Arrays.asList("BR1", "BR2").contains(reportName))
                ri.getItemIdentifier()
                    .removeIf(id -> id.getType().equals(IdentifierType.ONLINE_ISBN));
            })
        .flatMap(ri -> ri.getItemPerformance().stream())
        .map(Metric::getInstance)
        .forEach(
            list -> {
              if (Arrays.asList("JR1", "BR1").contains(reportName))
                list.removeIf(
                    pc ->
                        pc.getMetricType().equals(MetricType.FT_HTML)
                            || pc.getMetricType().equals(MetricType.FT_PDF));
            });
  }

  @Test
  public void testToReport() throws IOException, URISyntaxException, MapperException {
    String csvString = Resources.toString(Resources.getResource(expected), StandardCharsets.UTF_8);

    File file = new File(Resources.getResource(input).toURI());
    Report expectedReport =
        JAXB.unmarshal(file, CounterReportResponse.class).getReport().getReport().get(0);
    removeAttributes(expectedReport);

    Report parsedReport = MapperFactory.createCsvToReportMapper(csvString).toReport();
    assertThat(parsedReport)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields("vendor", "created", "id", "customer.webSiteUrl", "title")
        .isEqualTo(expectedReport);
  }
}
