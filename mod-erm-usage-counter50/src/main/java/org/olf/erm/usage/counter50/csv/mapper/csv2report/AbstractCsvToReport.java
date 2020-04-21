package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.olf.erm.usage.counter50.csv.mapper.MapperException;
import org.openapitools.client.model.SUSHIReportHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

public abstract class AbstractCsvToReport<T> implements CsvToReportMapper<T> {

  public static final int CONTENT_START_LINE = 14;
  private final Logger log = LoggerFactory.getLogger(AbstractCsvToReport.class);
  protected final List<String> lines;

  public AbstractCsvToReport(String csvString)
      throws MapperException, IOException {

    StringReader stringReader = new StringReader(csvString);
    lines = IOUtils.readLines(stringReader);

    if (lines.size() < CONTENT_START_LINE + 1) {
      throw new MapperException("Invalid report supplied");
    }
  }

  protected SUSHIReportHeader parseHeader() {
    Map<String, String> headerColumns = getHeaderColumns(lines.subList(0, 12));
    return CsvHeaderToReportHeader.parseHeader(headerColumns);
  }

  protected Map<String, String> getHeaderColumns(List<String> header) {
    try (CsvListReader csvListReader =
        new CsvListReader(
            new StringReader(StringUtils.join(header, System.lineSeparator())),
            CsvPreference.STANDARD_PREFERENCE)) {
      Map<String, String> headerColumn = new HashMap<>();
      List<String> line;
      while ((line = csvListReader.read()) != null) {
        headerColumn.put(line.get(0), line.get(1));
      }
      return headerColumn;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return Collections.emptyMap();
    }
  }

}
