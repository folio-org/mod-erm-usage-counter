package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import org.olf.erm.usage.counter41.csv.mapper.MapperException;

public class CsvToReportMapperFactory {

  public static CsvToReportMapper createCsvToReportMapper(String csvString) throws MapperException {

    if (true) {
      return new JR1Mapper(csvString);
    }
    throw new MapperException("exception");
  }

  private CsvToReportMapperFactory() {}
}
