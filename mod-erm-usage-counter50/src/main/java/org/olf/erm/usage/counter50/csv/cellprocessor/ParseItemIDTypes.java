package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.List;
import org.openapitools.client.model.COUNTERItemIdentifiers.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemIDTypes extends CellProcessorAdaptor {

  private final String[] header;

  public ParseItemIDTypes(String[] header) {
    this.header = header;
  }

  @Override
  public TypeEnum execute(Object value, CsvContext csvContext) {

    List<Object> rowSource = csvContext.getRowSource();
    rowSource.toString();

    if (value == null) {
      return null;
    }

    String typeString = header[csvContext.getColumnNumber()];
    return TypeEnum.fromValue(typeString);

  }
}
