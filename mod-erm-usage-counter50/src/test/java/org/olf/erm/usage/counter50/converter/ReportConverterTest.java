package org.olf.erm.usage.counter50.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.olf.erm.usage.counter50.converter.ReportConverter.ReportNotSupportedException;
import org.openapitools.counter50.model.COUNTERDatabaseReport;
import org.openapitools.counter50.model.COUNTERTitleReport;

public class ReportConverterTest {

  private static <T> T readReport(String path) throws IOException, Counter5UtilsException {
    String reportStr = Resources.toString(Resources.getResource(path), StandardCharsets.UTF_8);
    //noinspection unchecked
    return (T) Counter5Utils.fromJSON(reportStr);
  }

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

  @Test
  public void testThatTitleReportIsNotModified() throws IOException, Counter5UtilsException {
    COUNTERTitleReport tr = readReport("converter/tr/tr.json");
    COUNTERTitleReport unmodified = readReport("converter/tr/tr.json");

    new ReportConverter().convert(tr, "tr_j1");

    assertThat(tr).usingRecursiveComparison().isEqualTo(unmodified);
  }

  @Test
  public void testThatDatabaseReportIsNotModified() throws IOException, Counter5UtilsException {
    COUNTERDatabaseReport dr = readReport("converter/dr/dr.json");
    COUNTERDatabaseReport unmodified = readReport("converter/dr/dr.json");

    new ReportConverter().convert(dr, "dr_d1");

    assertThat(dr).usingRecursiveComparison().isEqualTo(unmodified);
  }
}
