package org.olf.erm.usage.counter50.merger;

import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;

public final class MergerFactory {

  private MergerFactory() {
  }

  public static <T> ReportsMerger createMerger(T report) throws Counter5UtilsException {
    if (report instanceof COUNTERTitleReport) {
      return new TRReportsMerger();
    } else if (report instanceof COUNTERPlatformReport) {
      return new PRReportsMerger();
    } else if (report instanceof COUNTERItemReport) {
      return new IRReportsMerger();
    } else if (report instanceof COUNTERDatabaseReport) {
      // merger = new DRReportsMerger();
      throw new Counter5UtilsException("Merging COUNTERDatabaseReport currently not supported.");
    } else {
      throw new Counter5UtilsException("Cannot merge reports. Unknown report type.");
    }
  }

}
