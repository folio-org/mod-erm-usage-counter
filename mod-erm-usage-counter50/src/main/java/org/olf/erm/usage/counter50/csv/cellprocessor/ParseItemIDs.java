package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemIDs extends CellProcessorAdaptor {

  private final String[] header;

  public ParseItemIDs(String[] header) {
    this.header = header;
  }

  @Override
  public COUNTERItemIdentifiers execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }
    COUNTERItemIdentifiers itemID = new COUNTERItemIdentifiers();
    String typeString = header[csvContext.getColumnNumber()];
    itemID.setType(TypeEnum.fromValue(typeString));
    itemID.setValue((String) value);
    return itemID;
//    return (String) value;
  }
}
