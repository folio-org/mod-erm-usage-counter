package org.olf.erm.usage.counter50.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter50.internal.CloneUtils.deepCopy;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERItemPerformanceInstance;
import org.openapitools.counter50.model.COUNTERItemPerformanceInstance.MetricTypeEnum;
import org.openapitools.counter50.model.COUNTERItemPerformancePeriod;
import org.openapitools.counter50.model.COUNTERTitleReport;
import org.openapitools.counter50.model.COUNTERTitleUsage;
import org.openapitools.counter50.model.SUSHIReportHeader;
import org.openapitools.counter50.model.SUSHIReportHeaderReportFilters;

public class CloneUtilsTest {

  private COUNTERTitleReport createTitleReport() {
    COUNTERItemPerformancePeriod period =
        new COUNTERItemPerformancePeriod().beginDate("2020-01-01").endDate("2020-01-31");
    COUNTERItemPerformanceInstance instance =
        new COUNTERItemPerformanceInstance()
            .metricType(MetricTypeEnum.TOTAL_ITEM_REQUESTS)
            .count(42);
    COUNTERItemPerformance performance =
        new COUNTERItemPerformance().period(period).instance(new ArrayList<>(List.of(instance)));
    COUNTERTitleUsage titleUsage =
        new COUNTERTitleUsage()
            .title("Some Title")
            .platform("Some Platform")
            .publisher("Some Publisher")
            .performance(new ArrayList<>(List.of(performance)));
    SUSHIReportHeader reportHeader =
        new SUSHIReportHeader()
            .reportID("TR")
            .release("5")
            .reportName("Title Master Report")
            .reportFilters(
                new ArrayList<>(
                    List.of(
                        new SUSHIReportHeaderReportFilters().name("Begin_Date").value("2020-01-01"),
                        new SUSHIReportHeaderReportFilters()
                            .name("End_Date")
                            .value("2020-01-31"))));
    return new COUNTERTitleReport()
        .reportHeader(reportHeader)
        .reportItems(new ArrayList<>(List.of(titleUsage)));
  }

  @Test
  public void testThatCopyIsEqualToButNotSameAsSource() {
    COUNTERTitleReport source = createTitleReport();

    COUNTERTitleReport copy = deepCopy(source);

    assertThat(copy).isEqualTo(source).isNotSameAs(source);
    assertThat(copy.getReportHeader()).isNotSameAs(source.getReportHeader());
    assertThat(copy.getReportItems()).isNotSameAs(source.getReportItems());
    assertThat(copy.getReportItems().get(0)).isNotSameAs(source.getReportItems().get(0));
  }

  @Test
  public void testThatModifyingCopyDoesNotAffectSource() {
    COUNTERTitleReport source = createTitleReport();
    COUNTERTitleReport unmodified = createTitleReport();

    COUNTERTitleReport copy = deepCopy(source);
    copy.getReportItems().get(0).getPerformance().get(0).getInstance().clear();
    copy.getReportItems().get(0).setTitle("Changed Title");
    copy.getReportHeader().getReportFilters().clear();
    copy.getReportItems().clear();

    assertThat(source).isEqualTo(unmodified);
  }

  @Test
  public void testThatRuntimeTypeIsPreserved() {
    Object source = createTitleReport();

    Object copy = deepCopy(source);

    assertThat(copy).isInstanceOf(COUNTERTitleReport.class).isEqualTo(source);
  }

  @Test
  public void testThatNullSourceThrowsException() {
    assertThatThrownBy(() -> deepCopy(null)).isInstanceOf(NullPointerException.class);
  }
}
