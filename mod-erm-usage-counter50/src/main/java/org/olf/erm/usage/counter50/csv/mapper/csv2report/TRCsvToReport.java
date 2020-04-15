package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import java.io.IOException;
import java.io.StringReader;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseAccessMethod;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseAccessType;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseDataType;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseItemIDs;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseMetricTypes;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParsePublisherID;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseSectionType;
import org.olf.erm.usage.counter50.csv.mapper.MapperException;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;
import org.openapitools.client.model.COUNTERTitleUsage.AccessMethodEnum;
import org.openapitools.client.model.COUNTERTitleUsage.AccessTypeEnum;
import org.openapitools.client.model.COUNTERTitleUsage.DataTypeEnum;
import org.openapitools.client.model.COUNTERTitleUsage.SectionTypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.dozer.CsvDozerBeanReader;
import org.supercsv.io.dozer.ICsvDozerBeanReader;
import org.supercsv.prefs.CsvPreference;

public class TRCsvToReport implements CsvToReportMapper<COUNTERTitleReport> {

  private static final int CONTENT_START_LINE = 14;
  private final Logger log = LoggerFactory.getLogger(TRCsvToReport.class);
  private final String csvString;

  public TRCsvToReport(String csvString) {
    this.csvString = csvString;
  }

  @Override
  public COUNTERTitleReport toReport() throws IOException, MapperException {
    COUNTERTitleReport result = new COUNTERTitleReport();

    StringReader stringReader = new StringReader(csvString);
    List<String> lines = IOUtils.readLines(stringReader);

    if (lines.size() < CONTENT_START_LINE + 1) {
      throw new MapperException("Invalid report supplied");
    }

    Map<String, String> headerColumns = getHeaderColumns(lines.subList(0, 12));
    SUSHIReportHeader sushiReportHeader = CsvHeaderToReportHeader.parseHeader(headerColumns);
    result.setReportHeader(sushiReportHeader);

    List<String> contentLines = lines.subList(CONTENT_START_LINE, lines.size());
    String titleUsagesString = String.join("\n", contentLines);

    List<COUNTERTitleUsage> counterTitleUsages = parseTitleUsages(sushiReportHeader,
        titleUsagesString);
    result.setReportItems(counterTitleUsages);
    return result;
  }

  private List<COUNTERTitleUsage> parseTitleUsages(SUSHIReportHeader reportHeader,
      String titleUsagesString)
      throws IOException {
    List<YearMonth> yearMonthsFromReportHeader = Counter5Utils
        .getYearMonthsFromReportHeader(reportHeader);

    List<COUNTERTitleUsage> counterTitleUsages = new ArrayList<>();
    try (ICsvDozerBeanReader beanReader = new CsvDozerBeanReader(
        new StringReader(titleUsagesString),
        CsvPreference.STANDARD_PREFERENCE)) {

      // the header elements are used to map the values to the bean (names must match)
      // the header columns are used as the keys to the Map
      final String[] fieldMapping = getFieldMapping(yearMonthsFromReportHeader);
      final CellProcessor[] processors = createProcessors(yearMonthsFromReportHeader);
      final Class<?>[] hintTypes = createHintTypes(yearMonthsFromReportHeader);
      beanReader.configureBeanMapping(COUNTERTitleUsage.class, fieldMapping, hintTypes);

      COUNTERTitleUsage ctu;
      while ((ctu = beanReader.read(COUNTERTitleUsage.class, processors)) != null) {
        counterTitleUsages.add(ctu);
      }

    }
    return mergeTitleUsages(counterTitleUsages);
  }

  private List<COUNTERTitleUsage> mergeTitleUsages(List<COUNTERTitleUsage> titleUsages) {
    // merge by itemID
    ArrayList<COUNTERTitleUsage> result = new ArrayList<>(
        titleUsages.stream()
            .collect(
                Collectors
                    .toMap(COUNTERTitleUsage::getItemID, tU -> tU, this::merge))
            .values());

    // remove null performances
    result.stream().forEach(ctu -> {
      List<COUNTERItemPerformance> perfsNotNull = ctu.getPerformance().stream()
          .filter(Objects::nonNull).collect(Collectors.toList());
      ctu.setPerformance(perfsNotNull);
    });

    // merge performances
    result.stream()
        .forEach(ctu -> {
              List<COUNTERItemPerformance> values = new ArrayList<>(
                  ctu.getPerformance()
                      .stream()
                      .collect(
                          Collectors.toMap(COUNTERItemPerformance::getPeriod,
                              counterItemPerformance -> counterItemPerformance,
                              this::mergeItemPerformances)).values());
              ctu.setPerformance(values);
            }
        );

    return result;
  }

  private Map<String, String> getHeaderColumns(List<String> header) {
    try (CsvListReader csvListReader =
        new CsvListReader(
            new StringReader(StringUtils.join(header, System.lineSeparator())),
            CsvPreference.STANDARD_PREFERENCE)) {
      Map<String, String> headerColumn = new HashMap<>();
      List<String> line;
      while ((line = csvListReader.read()) != null) {
        headerColumn.put(line.get(0), line.get(1));
      }
      return headerColumn;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return Collections.emptyMap();
    }
  }

  protected Class<?>[] createHintTypes(List<YearMonth> yearMonths) {
    Class<?>[] first = {
        null,
        null,
        null,
        null,
        COUNTERItemIdentifiers.class,
        COUNTERItemIdentifiers.class,
        COUNTERItemIdentifiers.class,
        COUNTERItemIdentifiers.class,
        COUNTERItemIdentifiers.class,
        COUNTERItemIdentifiers.class,
        DataTypeEnum.class,
        SectionTypeEnum.class,
        null,
        AccessTypeEnum.class,
        AccessMethodEnum.class,
        null,
        null
    };
    Stream<Class<COUNTERItemPerformance>> rest = yearMonths.stream()
        .map(ym -> COUNTERItemPerformance.class);
    return Stream.concat(Arrays.stream(first), rest).toArray(Class<?>[]::new);
  }


  protected CellProcessor[] createProcessors(List<YearMonth> yearMonths) {
    ParseItemIDs parseItemIDs = new ParseItemIDs(getHeader(yearMonths));
    ParseMetricTypes parseMetricTypes = new ParseMetricTypes(getHeader(yearMonths));

    List<CellProcessorAdaptor> first =
        Arrays.asList(
            new Optional(), // Title
            new Optional(), // Publisher
            new Optional(new ParsePublisherID()), // Publisher_ID
            new Optional(), // Platform
            new Optional(parseItemIDs), // DOI
            new Optional(parseItemIDs), // Proprietary Identifier
            new Optional(parseItemIDs), // ISBN
            new Optional(parseItemIDs), // Print ISSN
            new Optional(parseItemIDs), // Online ISSN
            new Optional(parseItemIDs), // URI
            new Optional(new ParseDataType()), // Data_Type
            new Optional(new ParseSectionType()), // Section_Type
            new Optional(), // YOP
            new Optional(new ParseAccessType()), // Access_Type
            new Optional(new ParseAccessMethod()), // Access_Method
            new Optional(), // Metric_Type
            new Optional() // Reporting_Period_Total
        );

    List<ParseMetricTypes> metricTypeParsers = Collections
        .nCopies(yearMonths.size(), parseMetricTypes);
    return Stream.concat(first.stream(), metricTypeParsers.stream()).toArray(CellProcessor[]::new);
  }

  public String[] getFieldMapping(List<YearMonth> yearMonths) {
    List<String> yearMonthHeaders = yearMonths.stream().map(YearMonth::toString)
        .collect(Collectors.toList());
    String[] y = yearMonthHeaders.toArray(new String[yearMonthHeaders.size()]);
    String[] baseHeader = new String[]{
        "Title",
        "Publisher",
        "PublisherID",
        "Platform",
        "itemID[0]",
        "itemID[1]",
        "itemID[2]",
        "itemID[3]",
        "itemID[4]",
        "itemID[5]",
        "DataType",
        "SectionType",
        "YOP",
        "AccessType",
        "AccessMethod",
        null,
        null
    };
    for (int i = 0; i < yearMonths.size(); i++) {
      y[i] = "performance[" + i + "]";
    }
    return Stream.concat(Arrays.stream(baseHeader), Arrays.stream(y)).toArray(String[]::new);
  }

  public String[] getHeader(List<YearMonth> yearMonths) {
    List<String> yearMonthHeaders = yearMonths.stream().map(YearMonth::toString)
        .collect(Collectors.toList());
    String[] y = yearMonthHeaders.toArray(new String[yearMonthHeaders.size()]);
    String[] baseHeader = new String[]{
        "Title",
        "Publisher",
        "Publisher_ID",
        "Platform",
        "DOI",
        "Proprietary",
        "ISBN",
        "Print_ISSN",
        "Online_ISSN",
        "URI",
        "Data_Type",
        "Section_Type",
        "YOP",
        "Access_Type",
        "Access_Method",
        "Metric_Type",
        "Reporting_Period_Total"
    };
    return Stream.concat(Arrays.stream(baseHeader), Arrays.stream(y)).toArray(String[]::new);
  }

  private COUNTERItemPerformance mergeItemPerformances(COUNTERItemPerformance a,
      COUNTERItemPerformance b) {
    b.getInstance().stream().forEach(i -> {
      if (i.getCount() != null) {
        List<COUNTERItemPerformanceInstance> list = new ArrayList<>(
            a.getInstance());
        list.add(i);
        a.setInstance(list);
      }
    });
    return a;
  }

  private COUNTERTitleUsage merge(COUNTERTitleUsage a, COUNTERTitleUsage b) {
    a.getPerformance().addAll(b.getPerformance());
    return a;
  }
}
