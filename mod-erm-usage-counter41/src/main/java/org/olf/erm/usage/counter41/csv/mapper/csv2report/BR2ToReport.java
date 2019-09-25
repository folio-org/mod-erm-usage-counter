package org.olf.erm.usage.counter41.csv.mapper.csv2report;

public class BR2ToReport extends BR1ToReport {

  @Override
  String getTitle() {
    return "Book Report 2";
  }

  @Override
  String getName() {
    return "BR2";
  }

  public BR2ToReport(String csvString) {
    super(csvString);
  }
}
