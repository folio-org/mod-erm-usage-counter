package org.olf.erm.usage.counter51;

import java.util.Arrays;
import java.util.List;

public enum ReportType {
  DR("Database Report"),
  DR_D1("Database Search and Item Usage"),
  DR_D2("Database Access Denied"),
  IR("Item Report"),
  IR_A1("Journal Article Requests"),
  IR_M1("Multimedia Item Requests"),
  PR("Platform Report"),
  PR_P1("Platform Usage"),
  TR("Title Report"),
  TR_B1("Book Requests (Controlled)"),
  TR_B2("Book Access Denied"),
  TR_B3("Book Usage by Access Type"),
  TR_J1("Journal Requests (Controlled)"),
  TR_J2("Journal Access Denied"),
  TR_J3("Journal Usage by Access Type"),
  TR_J4("Journal Requests by YOP (Controlled)");

  private final String reportName;

  ReportType(String reportName) {
    this.reportName = reportName;
  }

  public static List<ReportType> getStandardViews() {
    return Arrays.stream(ReportType.values()).filter(rt -> rt.toString().contains("_")).toList();
  }

  public static List<ReportType> getMasterReports() {
    return Arrays.stream(ReportType.values()).filter(rt -> !rt.toString().contains("_")).toList();
  }

  public String getReportName() {
    return reportName;
  }

  public ReportType getParentReportType() {
    return ReportType.valueOf(this.name().substring(0, 2));
  }

  ReportProperties getProperties() {
    return ReportProperties.valueOf(this.name());
  }

  public boolean isItemReport() {
    return this == IR || this == IR_A1 || this == IR_M1;
  }

  public boolean isStandardView() {
    return getStandardViews().contains(this);
  }
}
