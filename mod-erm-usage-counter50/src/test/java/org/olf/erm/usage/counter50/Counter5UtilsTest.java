package org.olf.erm.usage.counter50;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.openapitools.client.model.SUSHIReportHeader;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

@RunWith(Enclosed.class)
public class Counter5UtilsTest {

  @RunWith(Parameterized.class)
  public static class IsValidReportTest {

    @Parameters
    public static Collection<Object[]> data() throws IOException {
      return Arrays.asList(
          new Object[][] {
            {
              Resources.toString(Resources.getResource("hwire_pr.json"), StandardCharsets.UTF_8),
              true
            },
            {
              Resources.toString(Resources.getResource("hwire_trj1.json"), StandardCharsets.UTF_8),
              true
            },
            {"{}", false},
            {"[]", false},
            {"abc", false}
          });
    }

    @Parameter public String s;

    @Parameter(1)
    public boolean ex;

    @Test
    public void testIsValidReport() {
      SUSHIReportHeader header = null;
      try {
        header = Counter5Utils.getSushiReportHeader(s);
      } catch (Counter5UtilsException e) {
        // ignore
      }
      assertThat(Counter5Utils.isValidReportHeader(header)).isEqualTo(ex);
    }

    @Test
    public void testIsValidReport2() {
      SUSHIReportHeader header = null;
      try {
        header = Counter5Utils.getSushiReportHeader(s);
      } catch (Counter5UtilsException e) {
        // ignore -> header == null
      }
      assertThat(Counter5Utils.isValidReportHeader(header)).isEqualTo(ex);
    }
  }

  @RunWith(Parameterized.class)
  public static class GetYearMonthsFromReportHeaderTest {

    private static SUSHIReportHeader createHeader(String begin, String end) {
      SUSHIReportHeader h1 = new SUSHIReportHeader();
      SUSHIReportHeaderReportFilters h1f1 = new SUSHIReportHeaderReportFilters();
      h1f1.setName("Begin_Date");
      h1f1.setValue(begin);
      SUSHIReportHeaderReportFilters h1f2 = new SUSHIReportHeaderReportFilters();
      h1f2.setName("End_Date");
      h1f2.setValue(end);
      h1.setReportFilters(Arrays.asList(h1f1, h1f2));
      return h1;
    }

    @Parameters
    public static Collection<Object[]> data() {
      return Stream.of(
              new Object[][] {
                {createHeader("2019-01-01", "2019-12-31"), 12},
                {createHeader("2018-12-01", "2019-01-31"), 2},
                {createHeader("2018-12-01", "2018-12-31"), 1},
                {createHeader("2018-12-01", "2018-12-16"), 1},
                {createHeader("2018-12-31", "2018-01-01"), 0},
                {createHeader("2018-12-01", null), 0},
                {createHeader(null, "2018-12-31"), 0},
                {createHeader(null, null), 0},
              })
          .collect(Collectors.toList());
    }

    @Parameter public SUSHIReportHeader header;

    @Parameter(1)
    public int exSize;

    @Test
    public void testGetYearMonthsFromReportHeader() {
      List<YearMonth> yearMonths = Counter5Utils.getYearMonthsFromReportHeader(header);
      assertThat(yearMonths).hasSize(exSize);
    }
  }

  @RunWith(Parameterized.class)
  public static class SplitReportsTest {

    private final String input;

    public SplitReportsTest(String reportName) {
      this.input = "reports/" + reportName + ".json";
    }

    @Parameters
    public static Collection params() {
      return Arrays.asList("DR_merged", "IR_merged", "PR_merged", "TR_merged");
    }

    @Test
    public void testSplitReport() throws IOException, Counter5UtilsException {
      URL url = Resources.getResource(input);
      String jsonString = Resources.toString(url, StandardCharsets.UTF_8);
      Object report = Counter5Utils.fromJSON(jsonString);
      List splittedReports = Counter5Utils.split(report);

      SUSHIReportHeader sushiReportHeader = Counter5Utils.getSushiReportHeader(jsonString);
      List<YearMonth> yms = Counter5Utils.getYearMonthsFromReportHeader(sushiReportHeader);
      assertThat(splittedReports.size()).isEqualTo(yms.size());
    }
  }
}
