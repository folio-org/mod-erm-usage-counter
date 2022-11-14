package org.olf.erm.usage.counter41.csv.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.Test;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.Report.Customer;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.BR1;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.BR2;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.DB1;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.JR1;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.PR1;
import org.olf.erm.usage.counter41.csv.mapper.report2csv.ReportToCsvMapper;

public class MapperFactoryTest {

  @Test(expected = MapperException.class)
  public void testNoMappingForReport() throws MapperException {
    Report report = new Report();
    report.setVersion("4");
    report.setTitle("Report XYZ");
    MapperFactory.createCSVMapper(report).toCSV();
  }

  @Test(expected = MapperException.class)
  public void testNoTitle() throws MapperException {
    Report report = new Report();
    report.setVersion("4");
    MapperFactory.createCSVMapper(report).toCSV();
  }

  @Test(expected = MapperException.class)
  public void testNoVersion() throws MapperException {
    Report report = new Report();
    report.setTitle("Journal Report 1");
    MapperFactory.createCSVMapper(report).toCSV();
  }

  @Test
  public void testNoMappingForCsvToReport() {
    String s = "Journal Report 4 (R4)";
    assertThatThrownBy(() -> MapperFactory.createCsvToReportMapper(s))
        .hasMessage("Report type not supported");
  }

  private static Report createSampleReport(String title) {
    Report report = new Report();
    report.setVersion("4");
    report.setTitle(title);
    report.setCustomer(List.of(new Customer()));
    return report;
  }

  private static void assertThatReportToCsvFails(String title) {
    Report report = createSampleReport(title);
    assertThatThrownBy(() -> MapperFactory.createCSVMapper(report))
        .hasMessageContaining("No mapping found", title, "version '4'");
  }

  private static void assertThatReportToCsvSucceeds(String title, Class<?> clazz)
      throws MapperException {
    Report report = createSampleReport(title);
    ReportToCsvMapper csvMapper = MapperFactory.createCSVMapper(report);
    assertThat(csvMapper).isInstanceOf(clazz);
  }

  @Test
  public void testMappingsForReportToCsv() throws MapperException {
    assertThatReportToCsvSucceeds("BR1", BR1.class);
    assertThatReportToCsvSucceeds("BR1", BR1.class);
    assertThatReportToCsvSucceeds("Book Report 1", BR1.class);
    assertThatReportToCsvSucceeds("BR2", BR2.class);
    assertThatReportToCsvSucceeds("Book Report 2", BR2.class);
    assertThatReportToCsvSucceeds("DB1", DB1.class);
    assertThatReportToCsvSucceeds("Database Report 1", DB1.class);
    assertThatReportToCsvSucceeds("JR1", JR1.class);
    assertThatReportToCsvSucceeds("Journal Report 1", JR1.class);
    assertThatReportToCsvSucceeds("JR1:4", JR1.class);
    assertThatReportToCsvSucceeds(" JR1:4", JR1.class);
    assertThatReportToCsvFails("JR1a");
    assertThatReportToCsvFails("Journal Report 1a");
    assertThatReportToCsvFails("JR1 GOA");
    assertThatReportToCsvFails("Journal Report 1 GOA");
    assertThatReportToCsvSucceeds("PR1", PR1.class);
    assertThatReportToCsvSucceeds("Platform Report 1", PR1.class);
    assertThatReportToCsvFails("TR3 Mobile");
  }
}
