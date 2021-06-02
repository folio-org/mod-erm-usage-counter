package org.olf.erm.usage.counter50.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;

@RunWith(Parameterized.class)
public class TRConverterTest {

  private static final String RESOURCES_PATH = "converter/tr/";
  private static COUNTERTitleReport tr;
  private final ReportConverter reportConverter = new ReportConverter();
  private final BiPredicate<String, String> reportFilterValueFieldEquals =
      (s1, s2) -> Arrays.asList(s1.split("\\|")).containsAll(Arrays.asList(s2.split("\\|")));
  private final String reportType;

  public TRConverterTest(String reportType) {
    this.reportType = reportType;
  }

  @Parameters(name = "{0}")
  public static List<String> getReportTypes() {
    return List.of("tr_j1", "tr_j3", "tr_j4", "tr_b1", "tr_b3");
  }

  @BeforeClass
  public static void beforeClass() throws IOException, Counter5UtilsException {
    String trStr =
        Resources.toString(
            Resources.getResource(RESOURCES_PATH + "tr.json"), StandardCharsets.UTF_8);
    tr = (COUNTERTitleReport) Counter5Utils.fromJSON(trStr);
  }

  private void sortReportItems(COUNTERTitleReport report) {
    List<COUNTERTitleUsage> collect =
        report.getReportItems().stream()
            .map(
                tu -> {
                  tu.getPerformance()
                      .forEach(
                          p ->
                              p.setInstance(
                                  p.getInstance().stream()
                                      .sorted(
                                          Comparator.comparing(
                                              COUNTERItemPerformanceInstance::hashCode))
                                      .collect(Collectors.toList())));
                  return tu;
                })
            .sorted(Comparator.comparing(COUNTERTitleUsage::hashCode))
            .collect(Collectors.toList());
    report.setReportItems(collect);
  }

  private void assertThatReportsAreEqual(COUNTERTitleReport actual, COUNTERTitleReport expected) {
    sortReportItems(actual);
    sortReportItems(expected);

    assertThat(actual.getReportHeader())
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields("created")
        .withEqualsForFields(reportFilterValueFieldEquals, "reportFilters.value")
        .isEqualTo(expected.getReportHeader());
    assertThat(actual.getReportItems()).isEqualTo(expected.getReportItems());
  }

  @Test
  public void testConvert() throws IOException, Counter5UtilsException {
    COUNTERTitleReport actual = reportConverter.convert(tr, reportType);

    String expectedStr =
        Resources.toString(
            Resources.getResource(RESOURCES_PATH + reportType + ".json"), StandardCharsets.UTF_8);
    COUNTERTitleReport expected = (COUNTERTitleReport) Counter5Utils.fromJSON(expectedStr);

    assertThatReportsAreEqual(actual, expected);
  }
}
