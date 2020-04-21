package org.olf.erm.usage.counter50.csv.mapper;

import java.io.IOException;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.CsvToReportMapper;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.IRCsvToReport;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.TRCsvToReport;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.DR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.IR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.PR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.ReportToCsvMapper;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.TR;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;

public final class MapperFactory {

  private MapperFactory() {
  }

  public static ReportToCsvMapper createCSVMapper(Object report) throws MapperException {
    if (report instanceof COUNTERTitleReport) {
      return new TR((COUNTERTitleReport) report);
    } else if (report instanceof COUNTERPlatformReport) {
      return new PR((COUNTERPlatformReport) report);
    } else if (report instanceof COUNTERItemReport) {
      return new IR((COUNTERItemReport) report);
    } else if (report instanceof COUNTERDatabaseReport) {
      return new DR((COUNTERDatabaseReport) report);
    } else {
      throw new MapperException("Cannot create mapper");
    }
  }

  public static CsvToReportMapper createCsvToReportMapper(String csvReport) throws MapperException {
    try {
      if (csvReport.startsWith("Report_Name,Title Master Report")) {
        return new TRCsvToReport(csvReport);
      } else if (csvReport.startsWith("Report_Name,Item Master Report")) {
        return new IRCsvToReport(csvReport);
      }
      return null;
    } catch (IOException | MapperException e) {
      throw new MapperException("Cannot create CsvToReportMapper. " + e.getCause());
    }
  }
}
