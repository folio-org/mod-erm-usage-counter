package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.List;
import java.util.Objects;
import org.openapitools.client.model.COUNTERItemIdentifiers;

public final class IdentifierProcessor {

  private IdentifierProcessor() {
    //
  }

  public static String getValue(
      List<COUNTERItemIdentifiers> itemIds, COUNTERItemIdentifiers.TypeEnum identifier) {
    if (itemIds == null || itemIds.isEmpty() || identifier == null) {
      return null;
    }

    return itemIds.stream()
        .filter(Objects::nonNull)
        .filter(id -> identifier.equals(id.getType()))
        .findFirst()
        .map(COUNTERItemIdentifiers::getValue)
        .orElse(null);
  }
}
