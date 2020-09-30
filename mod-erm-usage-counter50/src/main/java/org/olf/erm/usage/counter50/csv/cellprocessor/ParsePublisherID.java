package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openapitools.client.model.COUNTERPublisherIdentifiers;
import org.openapitools.client.model.COUNTERPublisherIdentifiers.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParsePublisherID extends CellProcessorAdaptor {

  @Override
  public Object execute(Object value, CsvContext csvContext) {

    if (value == null) {
      return Collections.emptyList();
    }
    return createPublisherIdentifiers((String) value);
  }

  private List<COUNTERPublisherIdentifiers> createPublisherIdentifiers(String input) {
    String[] splitBySemicolon = input.split(";");
    List<COUNTERPublisherIdentifiers> publisherIdentifiers = new ArrayList<>();
    COUNTERPublisherIdentifiers previousPublisherIdentifier = null;
    for (String id : splitBySemicolon) {
      id = id.trim();
      String[] split = id.split("=");
      if (split.length == 1) {
        // if length is 1 there is no type and value is appended to previous identifier's value
        if (previousPublisherIdentifier != null) {
          previousPublisherIdentifier.setValue(previousPublisherIdentifier.getValue() + "; " + id);
        }
      } else {
        COUNTERPublisherIdentifiers cId = new COUNTERPublisherIdentifiers();
        cId.setType(TypeEnum.fromValue(split[0]));
        cId.setValue(split[1]);
        publisherIdentifiers.add(cId);
        previousPublisherIdentifier = cId;
      }
    }
    return publisherIdentifiers;
  }
}
