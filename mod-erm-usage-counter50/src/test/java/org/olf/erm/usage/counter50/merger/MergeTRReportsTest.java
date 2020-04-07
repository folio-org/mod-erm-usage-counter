package org.olf.erm.usage.counter50.merger;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;

public class MergeTRReportsTest extends MergerTest<COUNTERTitleReport> {

  @Before
  public void setUp() {
    prefix = "TR";
  }

  @Test
  public void testMergeReports() throws IOException, Counter5UtilsException {
    List<COUNTERTitleReport> reports = readData();
    COUNTERTitleReport mergedReports = Counter5Utils
        .merge(Arrays.asList(reports.get(0), reports.get(1), reports.get(2)));
    COUNTERTitleReport repExpected = reports.get(3);

    // Need to delete reportHeader.created as assertj does not ignore nested field reportHeader.created...
    mergedReports.getReportHeader().setCreated("");
    repExpected.getReportHeader().setCreated("");

    assertThat(mergedReports.getReportHeader()).usingRecursiveComparison()
        .ignoringCollectionOrder().isEqualTo(repExpected.getReportHeader());
    assertThat(mergedReports.getReportItems().size())
        .isEqualTo(repExpected.getReportItems().size());

    // Compare each platform usage
    for (int i = 0; i < mergedReports.getReportItems().size(); i++) {
      COUNTERTitleUsage counterTitleUsage = mergedReports.getReportItems().get(i);
      COUNTERTitleUsage expectedCounterTitleUsage = repExpected.getReportItems().stream()
          .filter(item -> item.getTitle().equals(counterTitleUsage.getTitle()))
          .findFirst().get();
      assertThat(counterTitleUsage).usingRecursiveComparison().ignoringCollectionOrder()
          .isEqualTo(expectedCounterTitleUsage);
    }
  }
}
