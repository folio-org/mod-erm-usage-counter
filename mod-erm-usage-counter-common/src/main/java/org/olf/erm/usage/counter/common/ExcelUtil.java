package org.olf.erm.usage.counter.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class ExcelUtil {

  private ExcelUtil() {}

  private static final CsvPreference CSV_PREF =
      new CsvPreference.Builder('"', ',', "\r\n").ignoreEmptyLines(false).build();

  public static String toCSV(InputStream inputStream) throws IOException {
    try (Workbook wb = WorkbookFactory.create(inputStream)) {
      if (wb.getNumberOfSheets() == 0) {
        throw new IOException("No sheets found.");
      }
      Sheet sheet = wb.getSheetAt(0);

      int lastRowNum = sheet.getLastRowNum();
      if (lastRowNum != -1) {
        List<List<String>> results =
            IntStream.rangeClosed(0, lastRowNum)
                .mapToObj(
                    rn -> {
                      Row row = sheet.getRow(rn);
                      int lastCellNum = row.getLastCellNum() - 1;
                      return IntStream.rangeClosed(0, lastCellNum)
                          .mapToObj(
                              cn -> {
                                Cell cell = row.getCell(cn, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                                if (cell.getCellType() == CellType.NUMERIC) {
                                  return String.valueOf((int) cell.getNumericCellValue());
                                } else {
                                  return cell.getStringCellValue();
                                }
                              })
                          .collect(Collectors.toList());
                    })
                .collect(Collectors.toList());

        StringWriter stringWriter = new StringWriter();
        CsvListWriter csvListWriter = new CsvListWriter(stringWriter, CSV_PREF);
        for (List<String> result : results) {
          csvListWriter.write(result);
        }
        csvListWriter.close();
        return StringUtils.removeEnd(stringWriter.toString(), CSV_PREF.getEndOfLineSymbols());
      } else {
        throw new IOException("No rows found in sheet.");
      }
    }
  }

  /**
   * Converts a csv formatted Counter Report to Microsoft xlsx format using Apache Poi. SXSSF
   * streaming extension is used that utilizes disk buffering to reduce memory consumption.
   *
   * @param csvString csv formatted string
   * @return XLSX workbook as InputStream
   * @throws IOException
   */
  public static InputStream fromCSV(String csvString) throws IOException {
    csvString = csvString.concat(CSV_PREF.getEndOfLineSymbols());
    CsvListReader csvListReader = new CsvListReader(new StringReader(csvString), CSV_PREF);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (SXSSFWorkbook wb =
        new SXSSFWorkbook(5000) {
          @Override
          public void close() throws IOException {
            this.dispose();
            super.close();
          }
        }) {
      Sheet sheet = wb.createSheet();
      CellStyle numberCellStyle = wb.createCellStyle();
      numberCellStyle.setDataFormat((short) 1);

      List<String> columns;
      int rowNo = 0;
      while ((columns = csvListReader.read()) != null) {
        Row row = sheet.createRow(rowNo++);
        AtomicInteger cellNo = new AtomicInteger();
        columns.forEach(
            s -> {
              Cell cell = row.createCell(cellNo.getAndIncrement());
              try {
                cell.setCellValue(Integer.parseInt(s));
                cell.setCellStyle(numberCellStyle);
              } catch (NumberFormatException e) {
                cell.setCellValue(s);
              }
            });
      }

      wb.write(baos);
      return new ByteArrayInputStream(baos.toByteArray());
    }
  }
}
