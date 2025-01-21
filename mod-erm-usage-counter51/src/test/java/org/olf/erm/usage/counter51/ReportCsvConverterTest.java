package org.olf.erm.usage.counter51;

import static org.apache.commons.csv.CSVFormat.TDF;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter51.TestUtil.TR_WITH_INVALID_REPORT_HEADER;
import static org.olf.erm.usage.counter51.TestUtil.assertThatReportLinesAreEqualIgnoringOrder;
import static org.olf.erm.usage.counter51.TestUtil.getLinesFromString;
import static org.olf.erm.usage.counter51.TestUtil.getObjectMapper;
import static org.olf.erm.usage.counter51.TestUtil.getResourcePath;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsLines;
import static org.olf.erm.usage.counter51.TestUtil.readFileAsObjectNode;
import static org.olf.erm.usage.counter51.TestUtil.removeBOMAndTrailingDelimiters;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.olf.erm.usage.counter51.ReportValidator.ReportValidatorException;

class ReportCsvConverterTest {

  private static final String SAMPLE_FILE_EXTENSION = "tsv";
  private static final ReportCsvConverter REPORT_CSV_CONVERTER =
      new ReportCsvConverter(getObjectMapper());

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

  @Test
  void testConvertReportToTSVWithInvalidReport() throws IOException {
    Path inputReportFilePath = getResourcePath(TR_WITH_INVALID_REPORT_HEADER);
    ObjectNode reportNode = readFileAsObjectNode(inputReportFilePath.toFile());

    StringWriter stringWriter = new StringWriter();
    assertThatThrownBy(() -> REPORT_CSV_CONVERTER.convert(reportNode, stringWriter, TDF))
        .isInstanceOf(ReportValidatorException.class);
  }
}
