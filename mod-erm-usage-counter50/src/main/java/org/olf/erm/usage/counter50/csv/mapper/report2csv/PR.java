package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import java.util.Arrays;
import java.util.List;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERPlatformUsage;

public class PR extends AbstractPRMapper {

  public PR(COUNTERPlatformReport report) {
    super(report);
  }

  @Override
  protected String[] getHeader() {
    return new String[] {"Platform", "Data_Type", "Access_Method"};
  }

  @Override
  protected List<Object> getValues(COUNTERPlatformUsage platformUsage) {
    return Arrays.asList(
        platformUsage.getPlatform(), platformUsage.getDataType(), platformUsage.getAccessMethod());
  }
}
