package org.olf.erm.usage.counter50.converter;

import io.vertx.core.json.Json;
import org.olf.erm.usage.counter50.converter.dr.DRD1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRB1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRB3Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ3Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ4Converter;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERTitleReport;

public class ReportConverter {

  @SuppressWarnings("rawtypes")
  public static Converter create(String reportID) {
    switch (reportID.toLowerCase()) {
      case "tr_j1":
        return new TRJ1Converter();
      case "tr_j3":
        return new TRJ3Converter();
      case "tr_j4":
        return new TRJ4Converter();
      case "tr_b1":
        return new TRB1Converter();
      case "tr_b3":
        return new TRB3Converter();
      case "dr_d1":
        return new DRD1Converter();
      default:
        throw new ReportNotSupportedException(
            String.format("Report '%s' is not supported.", reportID));
    }
  }

  public COUNTERTitleReport convert(COUNTERTitleReport report, String reportID) {
    COUNTERTitleReport clone = Json.decodeValue(Json.encode(report), COUNTERTitleReport.class);

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
    COUNTERDatabaseReport clone =
        Json.decodeValue(Json.encode(report), COUNTERDatabaseReport.class);

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
