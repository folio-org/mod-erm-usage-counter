package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERItemAttributes;
import org.openapitools.client.model.COUNTERItemAttributes.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemAttributes extends CellProcessorAdaptor {

  private final TypeEnum type;

  public ParseItemAttributes(TypeEnum type) {
    this.type = type;
  }

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }

    COUNTERItemAttributes result = new COUNTERItemAttributes();
    result.setType(type);
    result.setValue((String) value);
    return result;
  }
}
