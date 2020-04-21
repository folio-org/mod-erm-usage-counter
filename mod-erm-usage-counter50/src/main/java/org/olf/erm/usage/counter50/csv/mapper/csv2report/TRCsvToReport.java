package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseAccessMethod;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseAccessType;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseDataType;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseItemIDs;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseMetricTypes;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParsePublisherID;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParseSectionType;
import org.olf.erm.usage.counter50.csv.mapper.MapperException;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.merger.Merger;
import org.olf.erm.usage.counter50.csv.mapper.csv2report.merger.TitleUsageMerger;
import org.olf.erm.usage.counter50.csv.mapper.report2csv.ItemParser;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;
import org.openapitools.client.model.COUNTERTitleUsage.AccessMethodEnum;
import org.openapitools.client.model.COUNTERTitleUsage.AccessTypeEnum;
import org.openapitools.client.model.COUNTERTitleUsage.DataTypeEnum;
import org.openapitools.client.model.COUNTERTitleUsage.SectionTypeEnum;
import org.openapitools.client.model.SUSHIReportHeader;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class TRCsvToReport extends
    AbstractCsvToReport<COUNTERTitleReport> {

  private final ItemParser<COUNTERTitleUsage> itemParser;
  private final Merger<COUNTERTitleUsage> itemsMerger;

  public TRCsvToReport(String csvString) throws IOException, MapperException {
    super(csvString);
    itemParser = new ItemParser<>(COUNTERTitleUsage.class);
    itemsMerger = new TitleUsageMerger();
  }

  @Override
  public COUNTERTitleReport toReport() {

    COUNTERTitleReport result = new COUNTERTitleReport();
    SUSHIReportHeader sushiReportHeader = super.parseHeader();
    result.setReportHeader(sushiReportHeader);

    List<String> contentLines = lines.subList(CONTENT_START_LINE, lines.size());
    String titleUsagesString = String.join("\n", contentLines);
    List<YearMonth> yearMonths = Counter5Utils
        .getYearMonthsFromReportHeader(sushiReportHeader);

    List<COUNTERTitleUsage> titleUsages = itemParser
        .parseItems(titleUsagesString,
            createFieldMapping(yearMonths),
            createProcessors(yearMonths),
            createHintTypes(yearMonths));
    result.setReportItems(itemsMerger.mergeItems(titleUsages));
    return result;
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
    ParseMetricTypes parseMetricTypes = new ParseMetricTypes(getHeader(yearMonths));

    List<CellProcessorAdaptor> first =
        Arrays.asList(
            new Optional(), // Title
            new Optional(), // Publisher
            new Optional(new ParsePublisherID()), // Publisher_ID
            new Optional(), // Platform
            new Optional(new ParseItemIDs(TypeEnum.DOI)), // DOI
            new Optional(new ParseItemIDs(TypeEnum.PROPRIETARY)), // Proprietary Identifier
            new Optional(new ParseItemIDs(TypeEnum.ISBN)), // ISBN
            new Optional(new ParseItemIDs(TypeEnum.PRINT_ISSN)), // Print ISSN
            new Optional(new ParseItemIDs(TypeEnum.ONLINE_ISSN)), // Online ISSN
            new Optional(new ParseItemIDs((TypeEnum.URI))), // URI
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

  public String[] createFieldMapping(List<YearMonth> yearMonths) {
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

}
