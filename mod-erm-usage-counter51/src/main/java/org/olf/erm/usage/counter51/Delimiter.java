package org.olf.erm.usage.counter51;

/**
 * Enumeration of delimiter strings used in COUNTER 5.1 report processing. These delimiters are used
 * for formatting and parsing various string representations of report data elements.
 */
enum Delimiter {
  EQUALS("="),
  PIPE("|"),
  SEMICOLON_SPACE("; "),
  COLON(":"),
  ;

  private final String value;

  Delimiter(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
