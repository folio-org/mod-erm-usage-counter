package org.olf.erm.usage.counter41.csv.mapper;

import java.io.IOException;
import org.junit.Test;
import org.niso.schemas.counter.Report;

public class MapperFactoryTest {

  @Test(expected = MapperException.class)
  public void testNoMappingForReport() throws MapperException {
    Report report = new Report();
    report.setVersion("4");
    report.setTitle("Report XYZ");
    MapperFactory.createCSVMapper(report).toCSV();
  }

  @Test(expected = MapperException.class)
  public void testNoMappingForCsv() throws IOException, MapperException {
    String s = "Journal Report 4 (R4)";
    MapperFactory.createCsvToReportMapper(s).toReport();
  }
}
