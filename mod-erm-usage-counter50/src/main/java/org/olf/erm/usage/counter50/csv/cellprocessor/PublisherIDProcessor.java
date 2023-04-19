package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERPublisherIdentifiers;
import org.openapitools.client.model.COUNTERPublisherIdentifiers.TypeEnum;

public final class PublisherIDProcessor {

  private PublisherIDProcessor() {}

  public static String getPublisherID(List<COUNTERPublisherIdentifiers> identifiers) {
    if (identifiers == null || identifiers.isEmpty()) {
      return null;
    }
    return identifiers.stream()
        .map(
            id -> {
              if (TypeEnum.PROPRIETARY.equals(id.getType())) {
                return id.getValue();
              } else {
                return String.join(":", id.getType().getValue(), id.getValue());
              }
            })
        .collect(Collectors.joining("; "));
  }
}
