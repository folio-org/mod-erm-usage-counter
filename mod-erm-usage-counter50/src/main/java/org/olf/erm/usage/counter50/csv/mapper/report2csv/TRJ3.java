package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import static org.olf.erm.usage.counter50.csv.cellprocessor.IdentifierProcessor.getValue;
import static org.olf.erm.usage.counter50.csv.cellprocessor.PublisherIDProcessor.getPublisherID;

import java.util.Arrays;
import java.util.List;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.COUNTERTitleUsage;

public class TRJ3 extends AbstractTRMapper {

  public TRJ3(COUNTERTitleReport report) {
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
      "Print_ISSN",
      "Online_ISSN",
      "URI",
      "Access_Type"
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
        getValue(titleUsage.getItemID(), TypeEnum.PRINT_ISSN),
        getValue(titleUsage.getItemID(), TypeEnum.ONLINE_ISSN),
        getValue(titleUsage.getItemID(), TypeEnum.URI),
        titleUsage.getAccessType());
  }
}
