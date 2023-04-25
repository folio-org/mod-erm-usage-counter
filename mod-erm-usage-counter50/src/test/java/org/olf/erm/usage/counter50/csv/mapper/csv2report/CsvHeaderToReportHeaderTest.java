package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.CsvHeaderToReportHeader.CsvHeaderParseException;
import org.openapitools.client.model.SUSHIReportHeader;

public class CsvHeaderToReportHeaderTest {

  @Test
  public void testValidateRequiredHeaderAttribues() {
    SUSHIReportHeader header = new SUSHIReportHeader();
    assertThatThrownBy(() -> CsvHeaderToReportHeader.validateRequiredHeaderAttribues(header))
        .isInstanceOf(CsvHeaderParseException.class)
        .hasMessage("'Created' cant be null");
  }
}
