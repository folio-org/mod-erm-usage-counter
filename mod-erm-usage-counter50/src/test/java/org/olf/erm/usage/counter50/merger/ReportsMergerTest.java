package org.olf.erm.usage.counter50.merger;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.Test;
import org.openapitools.counter50.model.COUNTERTitleReport;
import org.openapitools.counter50.model.SUSHIReportHeader;
import org.openapitools.counter50.model.SUSHIReportHeaderReportAttributes;
import org.openapitools.counter50.model.SUSHIReportHeaderReportFilters;

public class ReportsMergerTest {

  public static class TestMerger extends ReportsMerger<COUNTERTitleReport> {

    @Override
    public COUNTERTitleReport merge(List<COUNTERTitleReport> reports) {
      return null;
    }
  }

  @Test
  public void testMergeHeadersEmpty() {
    SUSHIReportHeader header1 = new SUSHIReportHeader();
    SUSHIReportHeader header2 = new SUSHIReportHeader();
    assertThat(new TestMerger().mergeHeaders(asList(header1, header2)))
        .isEqualTo(header1)
        .isEqualTo(header2);
  }

  @Test
  public void testMergeHeadersNormalizesPipeDelimitedValues() {
    SUSHIReportHeaderReportAttributes attrToShow1 = new SUSHIReportHeaderReportAttributes();
    attrToShow1.setName("Attributes_To_Show");
    attrToShow1.setValue("Data_Type|Section_Type|YOP|Access_Type|Access_Method");

    SUSHIReportHeaderReportAttributes attrToShow2 = new SUSHIReportHeaderReportAttributes();
    attrToShow2.setName("Attributes_To_Show");
    attrToShow2.setValue("Access_Method|Access_Type|Data_Type|Section_Type|YOP");

    SUSHIReportHeaderReportAttributes excludeMonthly = new SUSHIReportHeaderReportAttributes();
    excludeMonthly.setName("Exclude_Monthly_Details");
    excludeMonthly.setValue("True");

    SUSHIReportHeaderReportFilters filter1 = new SUSHIReportHeaderReportFilters();
    filter1.setName("Metric_Type");
    filter1.setValue("Total_Item_Requests|Unique_Item_Requests");

    SUSHIReportHeaderReportFilters filter2 = new SUSHIReportHeaderReportFilters();
    filter2.setName("Metric_Type");
    filter2.setValue("Unique_Item_Requests|Total_Item_Requests");

    SUSHIReportHeader header1 = new SUSHIReportHeader();
    header1.setReportAttributes(List.of(attrToShow1, excludeMonthly));
    header1.setReportFilters(List.of(filter1));

    SUSHIReportHeader header2 = new SUSHIReportHeader();
    header2.setReportAttributes(List.of(attrToShow2, excludeMonthly));
    header2.setReportFilters(List.of(filter2));

    SUSHIReportHeader merged = new TestMerger().mergeHeaders(asList(header1, header2));
    assertThat(merged.getReportAttributes()).hasSize(2);
    assertThat(merged.getReportAttributes().get(0).getValue())
        .isEqualTo("Data_Type|Section_Type|YOP|Access_Type|Access_Method");
    assertThat(merged.getReportAttributes().get(1).getValue()).isEqualTo("True");
    assertThat(merged.getReportFilters()).hasSize(1);
    assertThat(merged.getReportFilters().get(0).getValue())
        .isEqualTo("Total_Item_Requests|Unique_Item_Requests");
  }

  @Test
  public void testMergeHeadersNull() {
    SUSHIReportHeader header1 = new SUSHIReportHeader();
    List<SUSHIReportHeader> headers = asList(header1, null);
    TestMerger testMerger = new TestMerger();
    assertThatThrownBy(() -> testMerger.mergeHeaders(headers))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> testMerger.mergeHeaders(null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
