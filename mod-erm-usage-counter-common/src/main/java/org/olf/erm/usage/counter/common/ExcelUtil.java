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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
                              cn ->
                                  row.getCell(cn, MissingCellPolicy.CREATE_NULL_AS_BLANK)
                                      .getStringCellValue())
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

  public static InputStream fromCSV(String csvString) throws IOException {
    csvString = csvString.concat(CSV_PREF.getEndOfLineSymbols());
    CsvListReader csvListReader = new CsvListReader(new StringReader(csvString), CSV_PREF);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();

      List<String> line;
      int rowNo = 0;
      while ((line = csvListReader.read()) != null) {
        Row row = sheet.createRow(rowNo++);
        AtomicInteger cellNo = new AtomicInteger();
        line.forEach(s -> row.createCell(cellNo.getAndIncrement()).setCellValue(s));
      }

      wb.write(baos);
      return new ByteArrayInputStream(baos.toByteArray());
    }
  }
}
