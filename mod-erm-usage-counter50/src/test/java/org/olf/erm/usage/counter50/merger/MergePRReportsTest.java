package org.olf.erm.usage.counter50.merger;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERPlatformUsage;

public class MergePRReportsTest extends MergerTest<COUNTERPlatformReport> {

  @Before
  public void setUp() {
    prefix = "PR";
  }

  @Test
  public void testMergeReports() throws IOException, Counter5UtilsException {
    List<COUNTERPlatformReport> reports = readData();
    COUNTERPlatformReport mergedReports = Counter5Utils
        .merge(Arrays.asList(reports.get(0), reports.get(1), reports.get(2)));
    COUNTERPlatformReport repExpected = reports.get(3);

    // Need to delete reportHeader.created as assertj does not ignore nested field reportHeader.created...
    mergedReports.getReportHeader().setCreated("");
    repExpected.getReportHeader().setCreated("");

    assertThat(mergedReports.getReportHeader()).usingRecursiveComparison()
        .ignoringCollectionOrder().isEqualTo(repExpected.getReportHeader());
    assertThat(mergedReports.getReportItems().size())
        .isEqualTo(repExpected.getReportItems().size());

    // Compare each platform usage
    for (int i = 0; i < mergedReports.getReportItems().size(); i++) {
      COUNTERPlatformUsage counterPlatformUsage = mergedReports.getReportItems().get(i);
      COUNTERPlatformUsage expectedCounterPlatformUsage = repExpected.getReportItems().stream()
          .filter(item -> item.getPlatform().equals(counterPlatformUsage.getPlatform()))
          .findFirst().get();
      assertThat(counterPlatformUsage).usingRecursiveComparison().ignoringCollectionOrder()
          .isEqualTo(expectedCounterPlatformUsage);
    }
  }
}
