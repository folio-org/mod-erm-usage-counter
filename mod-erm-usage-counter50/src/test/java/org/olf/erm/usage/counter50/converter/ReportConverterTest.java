package org.olf.erm.usage.counter50.converter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;
import org.olf.erm.usage.counter50.converter.ReportConverter.ReportNotSupportedException;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERTitleReport;

public class ReportConverterTest {

  @Test
  public void testThatUnsupportedReportThrowsException() {
    ReportConverter reportConverter = new ReportConverter();
    COUNTERTitleReport tr = new COUNTERTitleReport();
    COUNTERDatabaseReport dr = new COUNTERDatabaseReport();

    assertThatThrownBy(() -> reportConverter.convert(tr, "xx_xx"))
        .isInstanceOf(ReportNotSupportedException.class)
        .hasMessageContainingAll("xx_xx", "COUNTERTitleReport");
    assertThatThrownBy(() -> reportConverter.convert(dr, "xx_xx"))
        .isInstanceOf(ReportNotSupportedException.class)
        .hasMessageContainingAll("xx_xx", "COUNTERDatabaseReport");
  }
}
