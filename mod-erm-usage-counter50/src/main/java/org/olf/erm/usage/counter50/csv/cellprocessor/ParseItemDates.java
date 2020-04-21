package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERItemDates;
import org.openapitools.client.model.COUNTERItemDates.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemDates extends CellProcessorAdaptor {

  private TypeEnum type;

  public ParseItemDates(TypeEnum type) {
    this.type = type;
  }

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }

    COUNTERItemDates result = new COUNTERItemDates();
    result.setType(type);
    result.setValue((String) value);
    return result;
  }
}
