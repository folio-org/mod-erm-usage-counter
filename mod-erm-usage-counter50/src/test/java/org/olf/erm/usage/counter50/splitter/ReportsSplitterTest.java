package org.olf.erm.usage.counter50.splitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERTitleReport;
import org.openapitools.counter50.model.COUNTERTitleUsage;
import org.openapitools.counter50.model.SUSHIReportHeader;
import org.openapitools.counter50.model.SUSHIReportHeaderReportFilters;

@RunWith(Enclosed.class)
public class ReportsSplitterTest {

  @RunWith(Parameterized.class)
  public static class SampleReportTest {

    private final String reportName;

    public SampleReportTest(String reportName) {
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

  public static class MalformedReportTest {

    private COUNTERTitleReport createReportWithPerformance(COUNTERItemPerformance performance) {
      SUSHIReportHeaderReportFilters beginDate = new SUSHIReportHeaderReportFilters();
      beginDate.setName("Begin_Date");
      beginDate.setValue("2022-01-01");
      SUSHIReportHeaderReportFilters endDate = new SUSHIReportHeaderReportFilters();
      endDate.setName("End_Date");
      endDate.setValue("2022-02-28");

      return new COUNTERTitleReport()
          .reportHeader(
              new SUSHIReportHeader()
                  .reportID("TR")
                  .reportFilters(new ArrayList<>(List.of(beginDate, endDate))))
          .reportItems(
              new ArrayList<>(
                  List.of(
                      new COUNTERTitleUsage()
                          .title("Title")
                          .performance(new ArrayList<>(List.of(performance))))));
    }

    @Test
    public void testThatPerformanceWithoutPeriodFailsTheSplit() {
      COUNTERItemPerformance performanceWithoutPeriod = new COUNTERItemPerformance();
      COUNTERTitleReport report = createReportWithPerformance(performanceWithoutPeriod);

      assertThatThrownBy(() -> Counter5Utils.split(report))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Report item has a performance entry without a Period");
    }
  }
}
