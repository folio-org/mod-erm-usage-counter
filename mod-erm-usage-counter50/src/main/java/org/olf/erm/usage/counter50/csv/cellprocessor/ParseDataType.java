package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERTitleUsage.DataTypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseDataType extends CellProcessorAdaptor {

  @Override
  public DataTypeEnum execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }
    return DataTypeEnum.fromValue((String) value);
  }
}
