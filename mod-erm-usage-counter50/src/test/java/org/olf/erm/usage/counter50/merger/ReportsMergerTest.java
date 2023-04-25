package org.olf.erm.usage.counter50.merger;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.Test;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.SUSHIReportHeader;

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
