package org.olf.erm.usage.counter50.converter;

import com.google.gson.Gson;
import org.olf.erm.usage.counter50.converter.dr.DRD1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRB1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRB3Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ3Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ4Converter;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERTitleReport;

public class ReportConverter {

  private final Gson gson = new Gson();

  public COUNTERTitleReport convert(COUNTERTitleReport report, String reportID) {
    COUNTERTitleReport clone = gson.fromJson(gson.toJson(report), COUNTERTitleReport.class);

    Converter<COUNTERTitleReport> converter;
    switch (reportID.toLowerCase()) {
      case "tr_j1":
        converter = new TRJ1Converter();
        break;
      case "tr_j3":
        converter = new TRJ3Converter();
        break;
      case "tr_j4":
        converter = new TRJ4Converter();
        break;
      case "tr_b1":
        converter = new TRB1Converter();
        break;
      case "tr_b3":
        converter = new TRB3Converter();
        break;
      default:
        throw new ReportNotSupportedException(
            "Report " + reportID + " not supported for COUNTERTitleReport");
    }
    return converter.convert(clone);
  }

  public COUNTERDatabaseReport convert(COUNTERDatabaseReport report, String reportID) {
    COUNTERDatabaseReport clone = gson.fromJson(gson.toJson(report), COUNTERDatabaseReport.class);

    if ("dr_d1".equalsIgnoreCase(reportID)) {
      return new DRD1Converter().convert(clone);
    } else {
      throw new ReportNotSupportedException(
          "Report " + reportID + " not supported for COUNTERDatabaseReport");
    }
  }

  public static class ReportNotSupportedException extends RuntimeException {

    public ReportNotSupportedException(String message) {
      super(message);
    }
  }
}
