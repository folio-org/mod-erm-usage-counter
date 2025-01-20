package org.olf.erm.usage.counter51;

import static org.apache.commons.csv.CSVFormat.TDF;
import static org.olf.erm.usage.counter51.TestUtil.assertThatReportLinesAreEqualIgnoringOrder;
import static org.olf.erm.usage.counter51.TestUtil.getLinesFromString;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsLines;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsObjectNode;
import static org.olf.erm.usage.counter51.TestUtil.removeBOMAndTrailingDelimiters;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ReportCsvConverterTest {

  private static final String SAMPLE_FILE_EXTENSION = "tsv";
  private static final ReportCsvConverter REPORT_CSV_CONVERTER = new ReportCsvConverter();

  @ParameterizedTest
  @EnumSource(ReportType.class)
  void testConvertReportToTSV(ReportType reportType) throws IOException {
    ObjectNode reportNode = readFileAsObjectNode(getSampleReportPath(reportType).toFile());

    StringWriter stringWriter = new StringWriter();
    REPORT_CSV_CONVERTER.convert(reportNode, stringWriter, TDF);
    stringWriter.flush();

    List<String> actualLines = getLinesFromString(stringWriter.toString(), "\r\n");
    List<String> expectedLines =
        removeBOMAndTrailingDelimiters(
            readFileAsLines(getSampleReportPath(reportType, SAMPLE_FILE_EXTENSION)), "\t");

    assertThatReportLinesAreEqualIgnoringOrder(actualLines, expectedLines);
  }
}
