package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import static org.olf.erm.usage.counter50.csv.cellprocessor.IdentifierProcessor.getValue;
import static org.olf.erm.usage.counter50.csv.cellprocessor.PublisherIDProcessor.getPublisherID;

import java.util.Arrays;
import java.util.List;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERDatabaseUsage;
import org.openapitools.client.model.COUNTERItemIdentifiers;

public class DRD1 extends AbstractDRMapper {

  public DRD1(COUNTERDatabaseReport report) {
    super(report);
  }

  @Override
  protected String[] getHeader() {
    return new String[] {"Database", "Publisher", "Publisher_ID", "Platform", "Proprietary_ID"};
  }

  @Override
  protected List<Object> getValues(COUNTERDatabaseUsage dbUsage) {
    return Arrays.asList(
        dbUsage.getDatabase(),
        dbUsage.getPublisher(),
        getPublisherID(dbUsage.getPublisherID()),
        dbUsage.getPlatform(),
        getValue(dbUsage.getItemID(), COUNTERItemIdentifiers.TypeEnum.PROPRIETARY));
  }
}
