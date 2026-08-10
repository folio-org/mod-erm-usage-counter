package org.olf.erm.usage.counter50.splitter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;

@RunWith(Parameterized.class)
public class ReportsSplitterTest {

  private final String reportName;

  public ReportsSplitterTest(String reportName) {
    this.reportName = reportName;
  }

  @Parameters(name = "{0}")
  public static List<String> params() {
    return List.of("TR_merged", "DR_merged", "IR_merged", "PR_merged");
  }

  private Object readReport() throws IOException, Counter5UtilsException {
    String reportStr =
        Resources.toString(
            Resources.getResource("reports/" + reportName + ".json"), StandardCharsets.UTF_8);
    return Counter5Utils.fromJSON(reportStr);
  }

  @Test
  public void testThatSourceReportIsNotModified() throws IOException, Counter5UtilsException {
    Object report = readReport();
    Object unmodified = readReport();

    List<?> split = Counter5Utils.split(report);

    // guards against the assertion below passing vacuously on a single-month report
    assertThat(split).hasSizeGreaterThan(1);
    assertThat(report).usingRecursiveComparison().isEqualTo(unmodified);
  }

  @Test
  public void testThatSplitReportsAreIndependent() throws IOException, Counter5UtilsException {
    Object report = readReport();

    List<?> split = Counter5Utils.split(report);
    Object first = split.get(0);
    Object second = split.get(1);
    Object unmodifiedSecond = Counter5Utils.split(readReport()).get(1);

    Counter5Utils.getSushiReportHeaderFromReportObject(first).getReportFilters().clear();

    assertThat(second).usingRecursiveComparison().isEqualTo(unmodifiedSecond);
  }
}
