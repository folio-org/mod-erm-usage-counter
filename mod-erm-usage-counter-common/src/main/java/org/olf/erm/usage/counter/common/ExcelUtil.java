package org.olf.erm.usage.counter.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
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

public class ExcelUtil {

  private ExcelUtil() {}

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
                      if (row != null) {
                        int lastCellNum = row.getLastCellNum() - 1;
                        return IntStream.rangeClosed(0, lastCellNum)
                            .mapToObj(
                                cn -> {
                                  Cell cell = row.getCell(cn, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                                  if (cell.getCellType() == CellType.NUMERIC) {
                                    return String.valueOf((int) cell.getNumericCellValue());
                                  } else {
                                    return cell.getStringCellValue().isEmpty()
                                        ? null
                                        : cell.getStringCellValue();
                                  }
                                })
                            .collect(Collectors.toList());
                      } else {
                        return Collections.<String>emptyList();
                      }
                    })
                .collect(Collectors.toList());

        StringWriter stringWriter = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(stringWriter, CSVFormat.RFC4180)) {
            printer.printRecords(results);
        }
        return StringUtils.removeEnd(
            stringWriter.toString(), CSVFormat.RFC4180.getRecordSeparator());
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
    csvString = csvString.concat(CSVFormat.RFC4180.getRecordSeparator());
    CSVParser csvParser = CSVParser.parse(csvString, CSVFormat.RFC4180);

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

      int rowNo = 0;
      for (CSVRecord csvRecord : csvParser.getRecords()) {
        List<String> row = csvRecord.toList();
        Row sheetRow = sheet.createRow(rowNo++);
        int sheetCellNo = 0;
        for (String s : row) {
          Cell cell = sheetRow.createCell(sheetCellNo++);
          try {
            cell.setCellValue(Integer.parseInt(s));
            cell.setCellStyle(numberCellStyle);
          } catch (NumberFormatException e) {
            cell.setCellValue(s);
          }
        }
      }

      wb.write(baos);
      return new ByteArrayInputStream(baos.toByteArray());
    }
  }
}