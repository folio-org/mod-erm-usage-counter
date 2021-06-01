package org.olf.erm.usage.counter50.csv.mapper;

import java.io.IOException;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.CsvToReportMapper;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.DRCsvToReport;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.IRCsvToReport;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.PRCsvToReport;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.TRCsvToReport;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.DR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.DRD1;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.IR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.PR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.ReportToCsvMapper;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.TR;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.TRB1;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.TRB3;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.TRJ1;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.TRJ3;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.TRJ4;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;

public final class MapperFactory {

  private MapperFactory() {}

  public static ReportToCsvMapper createReportToCsvMapper(Object report) throws MapperException {
    if (report instanceof COUNTERTitleReport) {
      COUNTERTitleReport titleReport = (COUNTERTitleReport) report;
      switch (titleReport.getReportHeader().getReportID().toLowerCase()) {
        case "tr_b1":
          return new TRB1(titleReport);
        case "tr_b3":
          return new TRB3(titleReport);
        case "tr_j1":
          return new TRJ1(titleReport);
        case "tr_j3":
          return new TRJ3(titleReport);
        case "tr_j4":
          return new TRJ4(titleReport);
        default:
          return new TR(titleReport);
      }
    } else if (report instanceof COUNTERPlatformReport) {
      return new PR((COUNTERPlatformReport) report);
    } else if (report instanceof COUNTERItemReport) {
      return new IR((COUNTERItemReport) report);
    } else if (report instanceof COUNTERDatabaseReport) {
      COUNTERDatabaseReport databaseReport = (COUNTERDatabaseReport) report;
      if (databaseReport.getReportHeader().getReportID().equalsIgnoreCase("dr_d1")) {
        return new DRD1(databaseReport);
      } else {
        return new DR(databaseReport);
      }
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
      } else if (csvReport.startsWith("Report_Name,Platform Master Report")) {
        return new PRCsvToReport(csvReport);
      } else if (csvReport.startsWith("Report_Name,Database Master Report")) {
        return new DRCsvToReport(csvReport);
      } else {
        throw new MapperException("Cannot create CsvToReportMapper. Report has unknown name.");
      }
    } catch (IOException | MapperException e) {
      throw new MapperException("Cannot create CsvToReportMapper. " + e.getCause());
    }
  }
}
