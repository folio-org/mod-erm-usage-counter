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
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERDatabaseUsage;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;

@RunWith(Parameterized.class)
public class DRConverterTest {

  private static final String RESOURCES_PATH = "converter/dr/";
  private static COUNTERDatabaseReport dr;
  private final ReportConverter reportConverter = new ReportConverter();
  private final BiPredicate<String, String> reportFilterValueFieldEquals =
      (s1, s2) -> Arrays.asList(s1.split("\\|")).containsAll(Arrays.asList(s2.split("\\|")));
  private final String reportType;

  public DRConverterTest(String reportType) {
    this.reportType = reportType;
  }

  @Parameters(name = "{0}")
  public static List<String> getReportTypes() {
    return List.of("dr_d1");
  }

  @BeforeClass
  public static void beforeClass() throws IOException, Counter5UtilsException {
    String drStr =
        Resources.toString(
            Resources.getResource(RESOURCES_PATH + "dr.json"), StandardCharsets.UTF_8);
    dr = (COUNTERDatabaseReport) Counter5Utils.fromJSON(drStr);
  }

  private void sortReportItems(COUNTERDatabaseReport report) {
    List<COUNTERDatabaseUsage> collect =
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
            .sorted(Comparator.comparing(COUNTERDatabaseUsage::hashCode))
            .collect(Collectors.toList());
    report.setReportItems(collect);
  }

  private void assertThatReportsAreEqual(
      COUNTERDatabaseReport actual, COUNTERDatabaseReport expected) {
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
    COUNTERDatabaseReport actual = reportConverter.convert(dr, reportType);

    String expectedStr =
        Resources.toString(
            Resources.getResource(RESOURCES_PATH + reportType + ".json"), StandardCharsets.UTF_8);
    COUNTERDatabaseReport expected = (COUNTERDatabaseReport) Counter5Utils.fromJSON(expectedStr);

    assertThatReportsAreEqual(actual, expected);
  }
}
