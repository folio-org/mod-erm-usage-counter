package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERTitleUsage.AccessTypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseAccessType extends CellProcessorAdaptor {

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }

    return AccessTypeEnum.fromValue((String) value);
  }
}
