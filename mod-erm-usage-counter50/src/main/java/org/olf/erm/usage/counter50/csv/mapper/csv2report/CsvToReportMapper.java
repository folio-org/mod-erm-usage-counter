package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import java.io.IOException;
import org.olf.erm.usage.counter50.csv.mapper.MapperException;

public interface CsvToReportMapper<T> {

  T toReport() throws IOException, MapperException;

}
