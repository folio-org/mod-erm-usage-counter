package org.olf.erm.usage.counter41.csv.cellprocessor;

import org.niso.schemas.counter.Identifier;
import org.niso.schemas.counter.IdentifierType;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class IdentifierParser extends CellProcessorAdaptor {

  private final IdentifierType type;

  public IdentifierParser(IdentifierType type) {
    this.type = type;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Identifier execute(Object value, CsvContext context) {
    if (value == null) {
      return null;
    }
    Identifier identifier = new Identifier();
    identifier.setType(type);
    identifier.setValue((String) value);
    return identifier;
  }
}
