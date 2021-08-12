package org.olf.erm.usage.counter50.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;
import org.olf.erm.usage.counter50.converter.dr.DRD1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRB1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRB3Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ1Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ3Converter;
import org.olf.erm.usage.counter50.converter.tr.TRJ4Converter;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERTitleReport;

public class MissingAttributesTest {

  private static String trStr;
  private static String drStr;
  private static COUNTERTitleReport tr;
  private static COUNTERDatabaseReport dr;

  @BeforeClass
  public static void beforeClass() throws IOException, Counter5UtilsException {
    trStr =
        Resources.toString(Resources.getResource("converter/tr/tr.json"), StandardCharsets.UTF_8);
    drStr =
        Resources.toString(Resources.getResource("converter/dr/dr.json"), StandardCharsets.UTF_8);
  }

  @Before
  public void setUp() throws Counter5UtilsException {
    tr = (COUNTERTitleReport) Counter5Utils.fromJSON(trStr);
    dr = (COUNTERDatabaseReport) Counter5Utils.fromJSON(drStr);
  }

  @Test
  public void testThatConverterIsWorkingWithNullAttributesWhileFiltering() {
    tr.getReportItems().forEach(tu -> tu.setAccessMethod(null));
    dr.getReportItems().forEach(du -> du.setAccessMethod(null));
    Stream.of(
            new TRJ1Converter(),
            new TRJ3Converter(),
            new TRJ4Converter(),
            new TRB1Converter(),
            new TRB3Converter())
        .forEach(
            m ->
                assertThatCode(() -> assertThat(m.convert(tr).getReportItems()).isEmpty())
                    .doesNotThrowAnyException());
    assertThatCode(() -> assertThat(new DRD1Converter().convert(dr).getReportItems()).isEmpty())
        .doesNotThrowAnyException();
  }

  @Test
  public void testThatConverterIsWorkingWithNullAttributesWhileGrouping() {
    tr.getReportItems().forEach(tu -> tu.setPlatform(null));
    dr.getReportItems().forEach(du -> du.setPlatform(null));
    Stream.of(
            new TRJ1Converter(),
            new TRJ3Converter(),
            new TRJ4Converter(),
            new TRB1Converter(),
            new TRB3Converter())
        .forEach(
            m ->
                assertThatCode(() -> assertThat(m.convert(tr).getReportItems()).isNotEmpty())
                    .doesNotThrowAnyException());
    assertThatCode(() -> assertThat(new DRD1Converter().convert(dr).getReportItems()).isNotEmpty())
        .doesNotThrowAnyException();
  }
}
