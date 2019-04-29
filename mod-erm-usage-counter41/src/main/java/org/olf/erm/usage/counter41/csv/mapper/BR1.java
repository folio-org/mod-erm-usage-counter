package org.olf.erm.usage.counter41.csv.mapper;

import org.niso.schemas.counter.Report;

public class BR1 extends BR2 {

  public BR1(Report report) {
    super(report);
  }

  @Override
  public String getTitle() {
    return "Book Report 1 (R4)";
  }

  @Override
  public String getDescription() {
    return "Number of Successful Title Requests by Month and Title";
  }
}
