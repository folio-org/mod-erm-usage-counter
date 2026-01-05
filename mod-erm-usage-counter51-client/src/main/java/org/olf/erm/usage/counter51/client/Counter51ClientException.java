package org.olf.erm.usage.counter51.client;

/** Exception thrown by Counter51Client operations. */
public class Counter51ClientException extends RuntimeException {

  private final Integer statusCode;
  private final String responseBody;

  /** Create exception from parsing/network error. */
  public Counter51ClientException(String message, Throwable cause) {
    super(message, cause);
    this.statusCode = null;
    this.responseBody = null;
  }

  /** Create exception from HTTP error response. */
  public Counter51ClientException(String message, Integer statusCode, String responseBody) {
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
