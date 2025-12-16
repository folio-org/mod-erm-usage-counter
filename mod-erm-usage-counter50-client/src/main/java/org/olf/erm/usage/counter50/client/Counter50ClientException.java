package org.olf.erm.usage.counter50.client;

/**
 * Exception thrown by Counter50Client operations.
 */
public class Counter50ClientException extends RuntimeException {

  private final Integer statusCode;
  private final String responseBody;

  /**
   * Create exception from parsing/network error.
   */
  public Counter50ClientException(String message, Throwable cause) {
    super(message, cause);
    this.statusCode = null;
    this.responseBody = null;
  }

  /**
   * Create exception from HTTP error response.
   */
  public Counter50ClientException(String message, Integer statusCode, String responseBody) {
    super(message);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public String getResponseBody() {
    return responseBody;
  }
}
