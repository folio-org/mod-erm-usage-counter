package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import static org.olf.erm.usage.counter50.csv.cellprocessor.IdentifierProcessor.getValue;
import static org.olf.erm.usage.counter50.csv.cellprocessor.PublisherIDProcessor.getPublisherID;

import java.util.Arrays;
import java.util.List;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;

public class TR extends AbstractTRMapper {

  public TR(COUNTERTitleReport report) {
    super(report);
  }

  @Override
  String[] getHeader() {
    return new String[] {
      "Title",
      "Publisher",
      "Publisher_ID",
      "Platform",
      "DOI",
      "Proprietary_ID",
      "ISBN",
      "Print_ISSN",
      "Online_ISSN",
      "URI",
      "Data_Type",
      "Section_Type",
      "YOP",
      "Access_Type",
      "Access_Method"
    };
  }

  @Override
  List<Object> getValues(COUNTERTitleUsage titleUsage) {
    return Arrays.asList(
        titleUsage.getTitle(),
        titleUsage.getPublisher(),
        getPublisherID(titleUsage.getPublisherID()),
        titleUsage.getPlatform(),
        getValue(titleUsage.getItemID(), TypeEnum.DOI),
        getValue(titleUsage.getItemID(), TypeEnum.PROPRIETARY),
        getValue(titleUsage.getItemID(), TypeEnum.ISBN),
        getValue(titleUsage.getItemID(), TypeEnum.PRINT_ISSN),
        getValue(titleUsage.getItemID(), TypeEnum.ONLINE_ISSN),
        getValue(titleUsage.getItemID(), TypeEnum.URI),
        titleUsage.getDataType(),
        titleUsage.getSectionType(),
        titleUsage.getYOP(),
        titleUsage.getAccessType(),
        titleUsage.getAccessMethod());
  }
}
