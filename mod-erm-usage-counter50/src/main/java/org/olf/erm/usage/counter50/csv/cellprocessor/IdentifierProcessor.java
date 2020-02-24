package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.List;
import org.openapitools.client.model.COUNTERItemIdentifiers;

public final class IdentifierProcessor {

  private IdentifierProcessor() {
    //
  }

  public static String getValue(
      List<COUNTERItemIdentifiers> itemIds, COUNTERItemIdentifiers.TypeEnum identifier) {
    return itemIds.stream()
        .filter(id -> identifier.equals(id.getType()))
        .findFirst()
        .map(COUNTERItemIdentifiers::getValue)
        .orElse(null);
  }
}
