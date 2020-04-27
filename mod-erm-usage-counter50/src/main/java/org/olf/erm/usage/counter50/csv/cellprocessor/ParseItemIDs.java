package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemIDs extends CellProcessorAdaptor {

  private final TypeEnum type;

  public ParseItemIDs(TypeEnum type) {
    this.type = type;
  }

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }
    COUNTERItemIdentifiers itemID = new COUNTERItemIdentifiers();
    itemID.setType(type);
    itemID.setValue((String) value);
    return itemID;
  }
}
