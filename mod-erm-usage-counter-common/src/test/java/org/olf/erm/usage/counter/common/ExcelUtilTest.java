package org.olf.erm.usage.counter.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Resources;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class ExcelUtilTest {

  private String getResourceAsString(String file) throws IOException {
    return Resources.toString(Resources.getResource(file), StandardCharsets.UTF_8);
  }

  @Test
  public void testBackAndForth4JR1() throws IOException {
    String csvString = getResourceAsString("c4/JR1.csv");
    InputStream fromCSV = ExcelUtil.fromCSV(csvString);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(csvString).isEqualToNormalizingNewlines(toCSV);
  }

  @Test
  public void testBackAndForth4BR1() throws IOException {
    String csvString = getResourceAsString("c4/BR1.csv");
    InputStream fromCSV = ExcelUtil.fromCSV(csvString);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(csvString).isEqualToNormalizingNewlines(toCSV);
  }

  @Test
  public void testBackAndForth5PR1() throws IOException {
    String csvString = getResourceAsString("c5/PR_1.csv");
    InputStream fromCSV = ExcelUtil.fromCSV(csvString);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(csvString).isEqualToNormalizingNewlines(toCSV);
  }

  @Test
  public void testSimpleLine() throws IOException {
    String testStr = "a,b,c";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testEmptyString() throws IOException {
    String testStr = "";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testEmptyLine() throws IOException {
    String testStr = "\r\n";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testEmptyLinesInBetween() throws IOException {
    String testStr = "a,b,c\n\na2,b2,c2";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testEmptyLinesAtStart() throws IOException {
    String testStr = "\n\na,b,c";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testEmptyLinesAtEndLineFeed() throws IOException {
    String testStr = "a,b,c\n\n";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testEmptyLinesAtEndCarriageReturn() throws IOException {
    String testStr = "a,b,c\r\n\r\n";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testUnicodeChars() throws IOException {
    String testStr = "\u00D1,\u00E1,\u00DA,\u06A8";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testQuotes() throws IOException {
    String testStr = "\"a,b,c\",\"d,e,f\",\"g,h,i\"";
    InputStream fromCSV = ExcelUtil.fromCSV(testStr);
    String toCSV = ExcelUtil.toCSV(fromCSV);
    assertThat(toCSV).isEqualToNormalizingNewlines(testStr);
  }

  @Test
  public void testEmptyWorkbook() throws IOException {
    try (Workbook wb = new XSSFWorkbook()) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      wb.write(baos);
      assertThatThrownBy(() -> ExcelUtil.toCSV(new ByteArrayInputStream(baos.toByteArray())))
          .hasMessage("No sheets found.");
    }
  }

  @Test
  public void testEmptySheet() throws IOException {
    try (Workbook wb = new XSSFWorkbook()) {
      wb.createSheet();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      wb.write(baos);
      assertThatThrownBy(() -> ExcelUtil.toCSV(new ByteArrayInputStream(baos.toByteArray())))
          .hasMessage("No rows found in sheet.");
    }
  }
}
