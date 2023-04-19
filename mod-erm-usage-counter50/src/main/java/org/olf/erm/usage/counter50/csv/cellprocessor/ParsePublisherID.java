package org.olf.erm.usage.counter50.csv.cellprocessor;

import static java.lang.String.format;

import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
    return Splitter.on(";")
        .trimResults()
        .splitToStream(input)
        .map(
            identifier -> {
              String[] split = identifier.split(":", 2);
              if (split.length != 2) {
                throw new PublisherIDParseException(
                    format(
                        "Error parsing COUNTERPublisherIdentifiers from string '%s'", identifier));
              }
              try {
                return new COUNTERPublisherIdentifiers()
                    .type(TypeEnum.fromValue(split[0]))
                    .value(split[1]);
              } catch (IllegalArgumentException e) {
                return new COUNTERPublisherIdentifiers()
                    .type(TypeEnum.PROPRIETARY)
                    .value(identifier);
              }
            })
        .collect(Collectors.toList());
  }

  static class PublisherIDParseException extends RuntimeException {

    public PublisherIDParseException(String message) {
      super(message);
    }
  }
}
