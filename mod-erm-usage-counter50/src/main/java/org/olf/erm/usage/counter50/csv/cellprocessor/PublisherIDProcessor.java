package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.List;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERPublisherIdentifiers;

public final class PublisherIDProcessor {

  private PublisherIDProcessor() {
  }

  public static String getPublisherID(List<COUNTERPublisherIdentifiers> identifiers) {
    if (identifiers == null || identifiers.isEmpty()) {
      return null;
    }
    return identifiers.stream()
        .map(id -> String.format("%s=%s", id.getType(), id.getValue()))
        .collect(Collectors.joining("; "));
  }
}
