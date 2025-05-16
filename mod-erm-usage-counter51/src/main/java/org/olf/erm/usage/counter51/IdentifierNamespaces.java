package org.olf.erm.usage.counter51;

import java.util.List;

/**
 * Constants class containing lists of known identifier namespaces for different entity types in
 * COUNTER 5.1 reports. These namespaces are used to distinguish between permitted identifier
 * namespaces from proprietary ones.
 */
class IdentifierNamespaces {

  static final List<String> AUTHOR_IDENTIFIERS = List.of("ISNI", "ORCID");
  static final List<String> INSTITUTION_IDENTIFIERS = List.of("ISNI", "ROR", "ISIL", "OCLC");
  static final List<String> PUBLISHER_IDENTIFIERS = List.of("ISNI", "ROR");

  private IdentifierNamespaces() {}
}
