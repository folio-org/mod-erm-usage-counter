package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERTitleUsage.AccessMethodEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseAccessMethod extends CellProcessorAdaptor {

  @Override
  public AccessMethodEnum execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }
    return AccessMethodEnum.fromValue((String) value);
  }
}
