package org.olf.erm.usage.counter50;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olf.erm.usage.counter50.TestUtil.sort;

import com.google.common.io.Resources;
import java.io.IOException;
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
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;
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
  public static class SplitAndMergeTest<T> {

    private final String reportName;
    private final Class<T> clazz;

    public SplitAndMergeTest(String reportName, Class<T> clazz) {
      this.reportName = reportName;
      this.clazz = clazz;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> params() {
      return List.of(
          new Object[] {"TR_merged", COUNTERTitleReport.class},
          new Object[] {"TRJ1_merged", COUNTERTitleReport.class},
          new Object[] {"DR_merged", COUNTERDatabaseReport.class},
          new Object[] {"IR_merged", COUNTERItemReport.class},
          new Object[] {"PR_merged", COUNTERPlatformReport.class},
          new Object[] {"full/tr", COUNTERTitleReport.class},
          new Object[] {"full/dr", COUNTERDatabaseReport.class},
          new Object[] {"full/ir", COUNTERItemReport.class},
          new Object[] {"full/pr", COUNTERPlatformReport.class},
          new Object[] {"full/dr_with_empty_months", COUNTERDatabaseReport.class});
    }

    @Test
    public void testSplitAndMerge() throws IOException, Counter5UtilsException {
      String expectedReportStr =
          Resources.toString(
              Resources.getResource("reports/" + reportName + ".json"), StandardCharsets.UTF_8);
      T expectedReport = clazz.cast(Counter5Utils.fromJSON(expectedReportStr));

      List split = Counter5Utils.split(expectedReport);
      T mergedReport = clazz.cast(Counter5Utils.merge(split));

      sort(mergedReport);
      sort(expectedReport);
      assertThat(mergedReport)
          .usingRecursiveComparison()
          .withEqualsForFields(
              (List<?> a, List<?> e) ->
                  ((a == null || a.isEmpty()) && (e == null || e.isEmpty()))
                      || (a != null && a.equals(e)),
              "reportHeader.exceptions",
              "reportHeader.reportAttributes",
              "reportItems.itemID")
          .isEqualTo(expectedReport);
    }
  }

  public static class UpdatedSpecificationTest {
    @Test
    public void testDatabaseDataTypeForTitleUsage() throws IOException, Counter5UtilsException {
      String json =
          Resources.toString(Resources.getResource("wiley_tr.json"), StandardCharsets.UTF_8);
      assertThat(Counter5Utils.fromJSON(json)).isNotNull();
    }
  }
}
