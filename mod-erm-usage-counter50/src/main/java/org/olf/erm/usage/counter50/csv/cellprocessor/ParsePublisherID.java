package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openapitools.client.model.COUNTERPublisherIdentifiers;
import org.openapitools.client.model.COUNTERPublisherIdentifiers.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParsePublisherID extends CellProcessorAdaptor {


  @Override
  public List<COUNTERPublisherIdentifiers> execute(Object value, CsvContext csvContext) {

    if (value == null) {
      return null;
    }

    List<Object> rowSource = csvContext.getRowSource();
    rowSource.toString();

    List<COUNTERPublisherIdentifiers> publisherIdentifiers = new ArrayList<>();
    String valueAsString = (String) value;
    String[] splitBySemicolon = valueAsString.split(";");
    List<String> ids = Stream.of(splitBySemicolon).map(s -> s.trim())
        .collect(Collectors.toList());
    ids.stream().forEach(id -> {
      String[] split = id.split("=");
      COUNTERPublisherIdentifiers cId = new COUNTERPublisherIdentifiers();
      cId.setType(TypeEnum.fromValue(split[0]));
      cId.setValue(split[1]);
      publisherIdentifiers.add(cId);
    });

    return publisherIdentifiers;
  }
}