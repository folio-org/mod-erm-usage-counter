package org.olf.erm.usage.counter50.merger;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERItemUsage;

public class MergeIRReportsTest extends MergerTest<COUNTERItemReport> {

  @Before
  public void setUp() {
    prefix = "IR";
  }

  @Test
  public void testMergeReports() throws IOException, Counter5UtilsException {
    List<COUNTERItemReport> reports = readData();

    COUNTERItemReport mergedReports = Counter5Utils
        .merge(Arrays.asList(reports.get(0), reports.get(1), reports.get(2)));
    COUNTERItemReport repExpected = reports.get(3);

    // Need to delete reportHeader.created as assertj does not ignore nested field reportHeader.created...
    mergedReports.getReportHeader().setCreated("");
    repExpected.getReportHeader().setCreated("");

    assertThat(mergedReports.getReportHeader()).usingRecursiveComparison()
        .ignoringCollectionOrder().isEqualTo(repExpected.getReportHeader());
    assertThat(mergedReports.getReportItems().size())
        .isEqualTo(repExpected.getReportItems().size());

    // Compare each reportItem
    for (int i = 0; i < mergedReports.getReportItems().size(); i++) {
      COUNTERItemUsage counterItemUsage = mergedReports.getReportItems().get(i);
      COUNTERItemUsage expectedCounterItemUsage = repExpected.getReportItems().stream()
          .filter(item -> item.getItem().equals(counterItemUsage.getItem())).findFirst().get();
      assertThat(counterItemUsage).usingRecursiveComparison().ignoringCollectionOrder()
          .isEqualTo(expectedCounterItemUsage);
    }
  }
}
