package org.olf.erm.usage.counter50.csv.mapper;

import org.olf.erm.usage.counter50.csv.mapper.report2csv.IR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.PR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.ReportToCsvMapper;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.TR;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;

public final class MapperFactory {

  private MapperFactory() {}

  public static ReportToCsvMapper createCSVMapper(Object report) throws MapperException {
    if (report instanceof COUNTERTitleReport) {
      return new TR((COUNTERTitleReport) report);
    } else if (report instanceof COUNTERPlatformReport) {
      return new PR((COUNTERPlatformReport) report);
    } else if (report instanceof COUNTERItemReport) {
      return new IR((COUNTERItemReport) report);
    } else {
      throw new MapperException("Cannot create mapper");
    }
  }
}
