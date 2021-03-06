package org.olf.erm.usage.counter41;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.HijrahDate;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXB;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.BeforeClass;
import org.junit.Test;
import org.niso.schemas.counter.DateRange;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.Report.Customer;
import org.niso.schemas.counter.ReportItem;
import org.niso.schemas.sushi.counter.CounterReportResponse;
import org.olf.erm.usage.counter41.Counter4Utils.ReportMergeException;
import org.olf.erm.usage.counter41.Counter4Utils.ReportSplitException;

public class Counter4UtilsTest {

  private static String json1;
  private static String json2;

  @BeforeClass
  public static void init() throws IOException {
    json1 = Resources.toString(Resources.getResource("merge/json1.json"), StandardCharsets.UTF_8);
    json2 = Resources.toString(Resources.getResource("merge/json2.json"), StandardCharsets.UTF_8);
  }

  @Test
  public void testToXML() throws IOException {
    File file = new File(Resources.getResource("reportJSTOR.xml").getFile());
    Report fromXML = JAXB.unmarshal(file, Report.class);
    String toJSON = Counter4Utils.toJSON(fromXML);

    String read = Files.asCharSource(file, StandardCharsets.UTF_8).read();
    assertThat(Counter4Utils.toXML(fromXML)).isEqualToIgnoringWhitespace(read);
    assertThat(Counter4Utils.toXML(toJSON)).isEqualToIgnoringWhitespace(read);

    String invalidReport = "<tag>text</tag>";
    assertThat(Counter4Utils.toXML(invalidReport)).isNull();
    assertThat(Counter4Utils.toXML((Report) null)).isNull();
    assertThat(Counter4Utils.toXML((String) null)).isNull();
  }

  @Test
  public void testConversions() throws IOException {
    File file = new File(Resources.getResource("reportJSTOR.xml").getFile());

    Report fromXML = JAXB.unmarshal(file, Report.class);
    Report fromXML2 =
        Counter4Utils.fromString(Files.asCharSource(file, StandardCharsets.UTF_8).read());
    Report fromJSON = Counter4Utils.fromJSON(Counter4Utils.toJSON(fromXML));
    Report fromJSON2 = Counter4Utils.fromJSON(Counter4Utils.mapper.writeValueAsString(fromXML));

    assertThat(fromJSON).usingRecursiveComparison().isEqualTo(fromXML);
    assertThat(fromJSON).usingRecursiveComparison().isEqualTo(fromJSON2);
    assertThat(fromJSON).usingRecursiveComparison().isEqualTo(fromXML2);
  }

  @Test
  public void testGetNameForReportTitle() {
    assertThat(Counter4Utils.getNameForReportTitle("Journal Report 1 (R4)")).isEqualTo("JR1");
    assertThat(Counter4Utils.getNameForReportTitle("Journal Report 1)")).isEqualTo("JR1");
    assertThat(Counter4Utils.getNameForReportTitle("JR1")).isEqualTo("JR1");
    assertThat(Counter4Utils.getNameForReportTitle("some title with JR1")).isNull();

    assertThat(Counter4Utils.getNameForReportTitle("JR1 GOA")).isNull();
    assertThat(Counter4Utils.getNameForReportTitle("JR1 GOA (R4)")).isNull();

    assertThat(Counter4Utils.getNameForReportTitle("BR1")).isEqualTo("BR1");
    assertThat(Counter4Utils.getNameForReportTitle("Book Report 1")).isEqualTo("BR1");
    assertThat(Counter4Utils.getNameForReportTitle("Book Report 1 (R4)")).isEqualTo("BR1");

    assertThat(Counter4Utils.getNameForReportTitle("BR2")).isEqualTo("BR2");
    assertThat(Counter4Utils.getNameForReportTitle("Book Report 2")).isEqualTo("BR2");
    assertThat(Counter4Utils.getNameForReportTitle("Book Report 2 (R4)")).isEqualTo("BR2");

    assertThat(Counter4Utils.getNameForReportTitle("DB1")).isEqualTo("DB1");
    assertThat(Counter4Utils.getNameForReportTitle("Database Report 1")).isEqualTo("DB1");
    assertThat(Counter4Utils.getNameForReportTitle("Database Report 1 (R4)")).isEqualTo("DB1");

    assertThat(Counter4Utils.getNameForReportTitle("PR1")).isEqualTo("PR1");
    assertThat(Counter4Utils.getNameForReportTitle("Platform Report 1")).isEqualTo("PR1");
    assertThat(Counter4Utils.getNameForReportTitle("Platform Report 1 (R4)")).isEqualTo("PR1");

    assertThat(Counter4Utils.getNameForReportTitle("")).isNull();
    assertThat(Counter4Utils.getNameForReportTitle("a title that does not exist")).isNull();
  }

  @Test
  public void testGetTitlesForReportName() {
    assertThat(Counter4Utils.getTitlesForReportName("JR1"))
        .isEqualTo(
            Arrays.asList("(?=\\bJR1\\b)((?!GOA).)*", "(?=\\bJournal Report 1\\b)((?!GOA).)*"));
    assertThat(Counter4Utils.getTitlesForReportName("")).isNull();
    assertThat(Counter4Utils.getTitlesForReportName("a report name that does not exist")).isNull();
  }

  @Test
  public void testMergeReports() throws ReportMergeException {
    Report rep1 = Counter4Utils.fromJSON(new JsonObject(json1).getJsonObject("report").encode());
    Report rep2 = Counter4Utils.fromJSON(new JsonObject(json2).getJsonObject("report").encode());

    Report merge = Counter4Utils.merge(rep1, rep2);
    assertThat(merge.getCustomer()).isNotEmpty();
    List<ReportItem> reportItems = merge.getCustomer().get(0).getReportItems();
    assertThat(reportItems.size()).isEqualTo(3);
    assertThat(reportItems.get(0).getItemPerformance().size()).isEqualTo(2);
  }

  @Test
  public void testMergeReportsAttributesDontMatch() {
    Report rep1 = Counter4Utils.fromJSON(new JsonObject(json1).getJsonObject("report").encode());
    Report rep2 =
        Counter4Utils.fromJSON(
            new JsonObject(json2).getJsonObject("report").put("version", "5").encode());

    assertThatThrownBy(() -> Counter4Utils.merge(rep1, rep2))
        .isInstanceOf(ReportMergeException.class)
        .hasMessageContaining("attributes do not match");
  }

  @Test
  public void testMergeReportsInvalidCustomer() {
    Report rep1 = Counter4Utils.fromJSON(new JsonObject(json1).getJsonObject("report").encode());
    Report rep2 = Counter4Utils.fromJSON(new JsonObject(json2).getJsonObject("report").encode());
    rep2.getCustomer().add(new Customer());

    assertThatThrownBy(() -> Counter4Utils.merge(rep1, rep2))
        .isInstanceOf(ReportMergeException.class)
        .hasMessageContaining("invalid customer definitions");
  }

  @Test
  public void testSplitReports() throws ReportSplitException {
    Report report =
        JAXB.unmarshal(
                Resources.getResource("split/reportJSTOR-JR1-2018.xml"),
                CounterReportResponse.class)
            .getReport()
            .getReport()
            .get(0);

    List<Report> split = Counter4Utils.split(report);
    assertThat(split.size()).isEqualTo(4);

    assertThat(split)
        .allSatisfy(
            r -> {
              assertThat(r)
                  .usingRecursiveComparison()
                  .ignoringCollectionOrder()
                  .ignoringFields("customer.reportItems")
                  .isEqualTo(split.get(0));
              assertThat(
                      r.getCustomer().get(0).getReportItems().stream()
                          .flatMap(ri -> ri.getItemPerformance().stream())
                          .map(Metric::getPeriod)
                          .distinct()
                          .count())
                  .isEqualTo(1);
            });

    Metric metric =
        split.get(2).getCustomer().get(0).getReportItems().get(1).getItemPerformance().get(0);
    assertThat(metric.getPeriod().getBegin()).hasToString("2018-03-01");
    assertThat(metric.getInstance().get(2).getCount().intValue()).isEqualTo(8);
    Metric metric2 =
        split.get(3).getCustomer().get(0).getReportItems().get(1).getItemPerformance().get(0);
    assertThat(metric2.getPeriod().getEnd()).hasToString("2018-04-30");
    assertThat(metric2.getInstance().get(2).getCount().intValue()).isEqualTo(2);
  }

  @Test
  public void testSplitReports2() throws ReportSplitException {
    Report report =
        JAXB.unmarshal(Resources.getResource("split/reportJSTORMultiMonth.xml"), Report.class);

    List<Report> split = Counter4Utils.split(report);
    System.out.println(Json.encodePrettily(split.get(0)));

    assertThat(split)
        .hasSize(2)
        .allSatisfy(r -> assertThat(r.getCustomer().get(0).getReportItems()).hasSize(1));
  }

  @Test
  public void testSplitAndMergeReport() throws ReportSplitException, ReportMergeException {
    Report report =
        JAXB.unmarshal(
                Resources.getResource("split/reportJSTOR-JR1-2018.xml"),
                CounterReportResponse.class)
            .getReport()
            .getReport()
            .get(0);

    List<Report> splitReports = Counter4Utils.split(report);
    Report mergedReport = Counter4Utils.merge(splitReports);
    assertThat(report)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields("id", "created", "vendor")
        .isEqualTo(mergedReport);
  }

  @Test
  public void testToXMLGregorianCalendar() {
    YearMonth ym = YearMonth.of(2018, 7);
    XMLGregorianCalendar ymResult = Counter4Utils.toXMLGregorianCalendar(ym);
    assertThat(ymResult).isNotNull().hasToString("2018-07");

    LocalDate ld = LocalDate.of(2018, 7, 14);
    XMLGregorianCalendar ldResult = Counter4Utils.toXMLGregorianCalendar(ld);
    assertThat(ldResult).isNotNull().hasToString("2018-07-14");

    assertThat(Counter4Utils.toXMLGregorianCalendar(HijrahDate.now())).isNull();
  }

  @Test
  public void testGetDateRangeFromYearMonth() {
    DateRange dr = Counter4Utils.getDateRangeForYearMonth(YearMonth.of(2020, 2));
    assertThat(dr.getBegin())
        .isEqualTo(Counter4Utils.toXMLGregorianCalendar(LocalDate.of(2020, 2, 1)));
    assertThat(dr.getEnd())
        .isEqualTo(Counter4Utils.toXMLGregorianCalendar(LocalDate.of(2020, 2, 29)));

    DateRange dr2 = Counter4Utils.getDateRangeForYearMonth(YearMonth.of(2020, 3));
    assertThat(dr2.getBegin())
        .isEqualTo(Counter4Utils.toXMLGregorianCalendar(LocalDate.of(2020, 3, 1)));
    assertThat(dr2.getEnd())
        .isEqualTo(Counter4Utils.toXMLGregorianCalendar(LocalDate.of(2020, 3, 31)));
  }
}
