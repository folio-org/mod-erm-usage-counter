package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseAccessType;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseDataType;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseMetricTypes;
import org.olf.erm.usage.counter50.csv.mapper.MapperException;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.merger.Merger;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.merger.PlatformUsageMerger;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.ItemParser;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERPlatformUsage;
import org.openapitools.client.model.COUNTERTitleUsage.AccessTypeEnum;
import org.openapitools.client.model.COUNTERTitleUsage.DataTypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class PRCsvToReport extends AbstractCsvToReport {

  private final ItemParser<COUNTERPlatformUsage> itemParser;
  private final Merger<COUNTERPlatformUsage> itemsMerger;

  public PRCsvToReport(String csvString) throws IOException, MapperException {
    super(csvString);
    itemParser = new ItemParser<>(COUNTERPlatformUsage.class);
    itemsMerger = new PlatformUsageMerger();
  }

  @Override
  public COUNTERPlatformReport toReport() {
    COUNTERPlatformReport result = new COUNTERPlatformReport();
    SUSHIReportHeader sushiReportHeader = super.parseHeader();
    result.setReportHeader(sushiReportHeader);

    List<String> contentLines = lines.subList(CONTENT_START_LINE, lines.size());
    String platformUsagesString = String.join("\n", contentLines);
    List<YearMonth> yearMonths = Counter5Utils
        .getYearMonthsFromReportHeader(sushiReportHeader);

    List<COUNTERPlatformUsage> platformUsages = itemParser
        .parseItems(platformUsagesString,
            createFieldMapping(yearMonths),
            createProcessors(yearMonths),
            createHintTypes(yearMonths));
    result.setReportItems(itemsMerger.mergeItems(platformUsages));
    return result;
  }

  protected Class<?>[] createHintTypes(List<YearMonth> yearMonths) {
    Class<?>[] first = {
        null,
        DataTypeEnum.class,
        AccessTypeEnum.class,
        null,
        null
    };
    Stream<Class<COUNTERItemPerformance>> rest = yearMonths.stream()
        .map(ym -> COUNTERItemPerformance.class);
    return Stream.concat(Arrays.stream(first), rest).toArray(Class<?>[]::new);
  }

  protected CellProcessor[] createProcessors(List<YearMonth> yearMonths) {
    ParseMetricTypes parseMetricTypes = new ParseMetricTypes(getHeader(yearMonths));

    List<CellProcessorAdaptor> first =
        Arrays.asList(
            new Optional(), // Platform
            new Optional(new ParseDataType()), // Data_Type
            new Optional(new ParseAccessType()), // Access_Type
            new Optional(), // Metric_Type
            new Optional() // Reporting_Period_Total
        );

    List<ParseMetricTypes> metricTypeParsers = Collections
        .nCopies(yearMonths.size(), parseMetricTypes);
    return Stream.concat(first.stream(), metricTypeParsers.stream()).toArray(CellProcessor[]::new);
  }

  public String[] getHeader(List<YearMonth> yearMonths) {
    List<String> yearMonthHeaders = yearMonths.stream().map(YearMonth::toString)
        .collect(Collectors.toList());
    String[] y = yearMonthHeaders.toArray(new String[yearMonthHeaders.size()]);
    String[] baseHeader = new String[]{
        "Platform",
        "Data_Type",
        "Access_Method",
        "Metric_Type",
        "Reporting_Period_Total"
    };
    return Stream.concat(Arrays.stream(baseHeader), Arrays.stream(y)).toArray(String[]::new);
  }

  public String[] createFieldMapping(List<YearMonth> yearMonths) {
    List<String> yearMonthHeaders = yearMonths.stream().map(YearMonth::toString)
        .collect(Collectors.toList());
    String[] y = yearMonthHeaders.toArray(new String[yearMonthHeaders.size()]);
    String[] baseHeader = new String[]{
        "Platform",
        "DataType",
        "AccessMethod",
        null,
        null
    };
    for (int i = 0; i < yearMonths.size(); i++) {
      y[i] = "performance[" + i + "]";
    }
    return Stream.concat(Arrays.stream(baseHeader), Arrays.stream(y)).toArray(String[]::new);
  }
}
