package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERItemContributors;
import org.openapitools.client.model.COUNTERItemContributors.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemContributors extends CellProcessorAdaptor {

  private TypeEnum type;

  public ParseItemContributors(TypeEnum type) {
    this.type = type;
  }

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }

    COUNTERItemContributors result = new COUNTERItemContributors();
    result.setType(type);
    result.setName((String) value);
    return result;
  }
}
