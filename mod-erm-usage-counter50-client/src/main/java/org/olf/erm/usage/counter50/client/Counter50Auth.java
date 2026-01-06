package org.olf.erm.usage.counter50.client;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

/** SUSHI authentication configuration. Supports API key and/or Requestor ID authentication. */
public class Counter50Auth {

  private final String apiKey;
  private final String requestorId;

  /**
   * Create authentication config.
   *
   * @param apiKey API key (optional, can be null)
   * @param requestorId Requestor ID (optional, can be null)
   */
  public Counter50Auth(String apiKey, String requestorId) {
    this.apiKey = apiKey;
    this.requestorId = requestorId;
  }

  /** Apply authentication to HTTP request. Adds api_key and/or requestor_id query parameters. */
  public void applyAuth(HttpRequest<Buffer> request) {
    if (apiKey != null && !apiKey.isEmpty()) {
      request.addQueryParam("api_key", apiKey);
    }
    if (requestorId != null && !requestorId.isEmpty()) {
      request.addQueryParam("requestor_id", requestorId);
    }
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getRequestorId() {
    return requestorId;
  }
}
