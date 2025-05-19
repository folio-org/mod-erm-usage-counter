package org.olf.erm.usage.counter51;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olf.erm.usage.counter.common.ExcelUtil.toCSV;
import static org.olf.erm.usage.counter51.Counter51Utils.getDefaultObjectMapper;
import static org.olf.erm.usage.counter51.ReportJsonConverter.REPORTING_PERIOD_TOTAL;
import static org.olf.erm.usage.counter51.ReportType.IR;
import static org.olf.erm.usage.counter51.TestUtil.getSampleReportPath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.olf.erm.usage.counter51.ReportJsonConverter.ReportProcessingException;
import org.olf.erm.usage.counter51.ReportJsonConverter.UnknownReportException;
import org.olf.erm.usage.counter51.ReportJsonConverter.UnsupportedReportException;
import org.olf.erm.usage.counter51.ReportValidator.ReportValidatorException;

class ReportJsonConverterTest {

  static final String EXTENSION_JSON = "json";
  static final String EXTENSION_TSV = "tsv";
  static final String EXTENSION_XLSX = "xlsx";
  private final ObjectMapper objectMapper = getDefaultObjectMapper();
  private final ReportJsonConverter converter = new ReportJsonConverter(objectMapper);

  @ParameterizedTest
  @EnumSource(
      value = ReportType.class,
      names = {"TR", "IR", "PR", "DR"})
  void testConvertTsvToJson(ReportType reportType) throws IOException {
    Path tsvPath = getSampleReportPath(reportType, EXTENSION_TSV);
    Path jsonPath = getSampleReportPath(reportType, EXTENSION_JSON);
    JsonNode actual = converter.convert(Files.newBufferedReader(tsvPath), CSVFormat.TDF);
    JsonNode expected = objectMapper.readTree(jsonPath.toFile());

    if (reportType == IR) {
      // IR sample has a different collection order
      assertThat(actual).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expected);
    } else {
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @EnumSource(
      value = ReportType.class,
      names = {"TR", "IR", "PR", "DR"})
  void testConvertXlsxToJson(ReportType reportType) throws IOException {
    Path tsvPath = getSampleReportPath(reportType, EXTENSION_XLSX);
    Path jsonPath = getSampleReportPath(reportType, EXTENSION_JSON);
    String csv = toCSV(Files.newInputStream(tsvPath, StandardOpenOption.READ));
    JsonNode actual = converter.convert(new StringReader(csv), CSVFormat.RFC4180);
    JsonNode expected = objectMapper.readTree(jsonPath.toFile());

    if (reportType == IR) {
      // IR sample has a different collection order
      assertThat(actual).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expected);
    } else {
      assertThat(actual).isEqualTo(expected);
    }
  }

  @Test
  void testConvertUnsupportedReport() throws IOException {
    Path tsvPath = getSampleReportPath(ReportType.TR_J1, EXTENSION_TSV);

    BufferedReader reader = Files.newBufferedReader(tsvPath);
    assertThatThrownBy(() -> converter.convert(reader, CSVFormat.TDF))
        .isInstanceOf(UnsupportedReportException.class);
  }

  @Test
  void testUnknowReport() {
    String sampleReport = "first row\nReport_ID,";

    StringReader reader = new StringReader(sampleReport);
    assertThatThrownBy(() -> converter.convert(reader, CSVFormat.RFC4180))
        .isInstanceOf(UnknownReportException.class);
  }

  @Test
  void testInvalidReportAttributes() throws IOException {
    Path tsvPath = getSampleReportPath(ReportType.TR, EXTENSION_TSV);

    // Modify the sample report to have an invalid report header
    String targetStr = "Attributes_To_Show=YOP|Access_Type|Access_Method";
    String replacementStr = "Attributes_To_Show=YOP";
    String input = Files.readString(tsvPath);
    String modifiedInput = input.replace(targetStr, replacementStr);

    assertThat(input).contains(targetStr);
    assertThat(modifiedInput).contains(replacementStr);
    StringReader reader = new StringReader(modifiedInput);
    assertThatThrownBy(() -> converter.convert(reader, CSVFormat.TDF))
        .isInstanceOf(ReportValidatorException.class);
  }

  @Test
  void testNoReportID() {
    String sampleReport = "first row\nsecond row";

    StringReader reader = new StringReader(sampleReport);
    assertThatThrownBy(() -> converter.convert(reader, CSVFormat.RFC4180))
        .isInstanceOf(ReportProcessingException.class)
        .hasMessageContaining("Report_ID");
  }

  @Test
  void testInvalidReportData() throws IOException {
    Path tsvPath = getSampleReportPath(ReportType.TR, EXTENSION_TSV);

    // Modify the sample report have no Reporting_Period_Total column in its data header
    String targetStr = REPORTING_PERIOD_TOTAL + CSVFormat.TDF.getDelimiterString();
    String replacementStr = "";
    String input = Files.readString(tsvPath);
    String modifiedInput = input.replace(targetStr, replacementStr);

    assertThat(input).contains(targetStr);
    assertThat(modifiedInput).doesNotContain(targetStr);
    StringReader reader = new StringReader(modifiedInput);
    assertThatThrownBy(() -> converter.convert(reader, CSVFormat.TDF))
        .isInstanceOf(ReportProcessingException.class);
  }
}
