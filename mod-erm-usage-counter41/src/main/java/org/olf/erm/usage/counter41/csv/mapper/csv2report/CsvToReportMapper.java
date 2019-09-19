package org.olf.erm.usage.counter41.csv.mapper.csv2report;

import java.io.IOException;
import org.niso.schemas.counter.Report;
import org.olf.erm.usage.counter41.csv.mapper.MapperException;

public interface CsvToReportMapper {

  Report toReport() throws IOException, MapperException;
}
