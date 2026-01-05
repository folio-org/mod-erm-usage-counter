package org.olf.erm.usage.counter51.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import java.util.List;
import org.olf.erm.usage.counter51.Counter51Utils;
import org.openapitools.counter51.model.DR;
import org.openapitools.counter51.model.IR;
import org.openapitools.counter51.model.Member;
import org.openapitools.counter51.model.PR;
import org.openapitools.counter51.model.Report;
import org.openapitools.counter51.model.Status;
import org.openapitools.counter51.model.TR;

/**
 * Vert.x HTTP client for COUNTER 5.1 SUSHI API. Provides Future-based async API for fetching
 * COUNTER reports.
 */
public class Counter51Client implements AutoCloseable {

  private static final ObjectMapper MAPPER = createObjectMapper();

  protected final WebClient client;
  protected final boolean ownsClient;
  protected final String baseUrl;
  protected final Counter51Auth auth;

  /**
   * Primary constructor - all initialization happens here.
   *
   * @param client WebClient instance
   * @param baseUrl Base URL of SUSHI service
   * @param auth Authentication credentials
   * @param ownsClient Whether this instance owns the WebClient (and should close it)
   */
  private Counter51Client(
      WebClient client, String baseUrl, Counter51Auth auth, boolean ownsClient) {
    this.client = client;
    this.ownsClient = ownsClient;
    // Remove all trailing slashes
    String normalizedUrl = baseUrl.replaceAll("/+$", "");
    // Add /r51 suffix if not already present (case-insensitive check)
    if (!normalizedUrl.toLowerCase().endsWith("/r51")) {
      normalizedUrl += "/r51";
    }
    this.baseUrl = normalizedUrl;
    this.auth = auth != null ? auth : new Counter51Auth(null, null);
  }

  /**
   * Public constructor - accepts shared WebClient (RECOMMENDED for production). Caller is
   * responsible for WebClient lifecycle management. The WebClient will NOT be closed when this
   * client is closed.
   *
   * @param client Shared WebClient instance
   * @param baseUrl Base URL of SUSHI service
   * @param auth Authentication credentials
   */
  public Counter51Client(WebClient client, String baseUrl, Counter51Auth auth) {
    this(client, baseUrl, auth, false);
  }

  /**
   * Convenience constructor - creates internal WebClient with default options. Use for simple
   * scenarios. For production with multiple clients, prefer passing a shared WebClient instance.
   * The WebClient will be closed when this client is closed.
   *
   * @param vertx Vert.x instance
   * @param baseUrl Base URL of SUSHI service
   * @param auth Authentication credentials
   */
  public Counter51Client(Vertx vertx, String baseUrl, Counter51Auth auth) {
    this(vertx, new WebClientOptions().setFollowRedirects(true), baseUrl, auth);
  }

  /**
   * Convenience constructor - creates internal WebClient with custom options. Use for simple
   * scenarios. For production with multiple clients, prefer passing a shared WebClient instance.
   * The WebClient will be closed when this client is closed.
   *
   * @param vertx Vert.x instance
   * @param options WebClient options
   * @param baseUrl Base URL of SUSHI service
   * @param auth Authentication credentials
   */
  public Counter51Client(
      Vertx vertx, WebClientOptions options, String baseUrl, Counter51Auth auth) {
    this(WebClient.create(vertx, options), baseUrl, auth, true);
  }

  private static ObjectMapper createObjectMapper() {
    return Counter51Utils.getDefaultObjectMapper();
  }

  /**
   * Fetch Title Report (TR).
   *
   * @param customerId Customer ID
   * @param beginDate Begin date (YYYY-MM-DD)
   * @param endDate End date (YYYY-MM-DD)
   * @param platform Platform (optional)
   * @return Future with report data
   */
  public Future<TR> getReportsTR(
      String customerId, String beginDate, String endDate, String platform) {
    return makeRequest(
        "/reports/tr",
        customerId,
        beginDate,
        endDate,
        platform,
        "YOP|Access_Type|Access_Method",
        null,
        TR.class);
  }

  /**
   * Fetch Item Report (IR).
   *
   * @param customerId Customer ID
   * @param beginDate Begin date (YYYY-MM-DD)
   * @param endDate End date (YYYY-MM-DD)
   * @param platform Platform (optional)
   * @return Future with report data
   */
  public Future<IR> getReportsIR(
      String customerId, String beginDate, String endDate, String platform) {
    return makeRequest(
        "/reports/ir",
        customerId,
        beginDate,
        endDate,
        platform,
        "Authors|Publication_Date|Article_Version|YOP|Access_Type|Access_Method",
        "True",
        IR.class);
  }

  /**
   * Fetch Database Report (DR).
   *
   * @param customerId Customer ID
   * @param beginDate Begin date (YYYY-MM-DD)
   * @param endDate End date (YYYY-MM-DD)
   * @param platform Platform (optional)
   * @return Future with report data
   */
  public Future<DR> getReportsDR(
      String customerId, String beginDate, String endDate, String platform) {
    return makeRequest(
        "/reports/dr", customerId, beginDate, endDate, platform, "Access_Method", null, DR.class);
  }

  /**
   * Fetch Platform Report (PR).
   *
   * @param customerId Customer ID
   * @param beginDate Begin date (YYYY-MM-DD)
   * @param endDate End date (YYYY-MM-DD)
   * @param platform Platform (optional)
   * @return Future with report data
   */
  public Future<PR> getReportsPR(
      String customerId, String beginDate, String endDate, String platform) {
    return makeRequest(
        "/reports/pr", customerId, beginDate, endDate, platform, "Access_Method", null, PR.class);
  }

  /**
   * Get server status.
   *
   * <p>Returns the current status of the reporting service(s) supported by the COUNTER API server.
   * This endpoint is public and does not require authentication.
   *
   * @param platform Platform (optional)
   * @return Future with server status
   */
  public Future<Status> getStatus(String platform) {
    HttpRequest<Buffer> request = client.getAbs(baseUrl + "/status");

    if (platform != null && !platform.isEmpty()) {
      request.addQueryParam("platform", platform);
    }

    return request.send().compose(response -> handleResponse(response, Status.class));
  }

  /**
   * Get list of consortium members.
   *
   * <p>Returns the institutions associated with the customer ID in the request and their customer
   * IDs and requestor IDs. The associated institutions can be consortium members, or sites for
   * multi-site customers. If the customer is neither a consortium nor multi-site, the list only
   * includes the details for the customer itself.
   *
   * @param customerId Customer ID (required)
   * @param platform Platform (optional)
   * @return Future with list of members
   */
  public Future<List<Member>> getMembers(String customerId, String platform) {
    HttpRequest<Buffer> request =
        client.getAbs(baseUrl + "/members").addQueryParam("customer_id", customerId);

    if (platform != null && !platform.isEmpty()) {
      request.addQueryParam("platform", platform);
    }

    auth.applyAuth(request);

    return request
        .send()
        .compose(
            response -> {
              int statusCode = response.statusCode();
              Buffer body = response.body();

              if (statusCode >= 200 && statusCode < 300) {
                try {
                  Member[] members = MAPPER.readValue(body.getBytes(), Member[].class);
                  return Future.succeededFuture(List.of(members));
                } catch (Exception e) {
                  return Future.failedFuture(
                      new Counter51ClientException(
                          "Failed to parse members response: " + e.getMessage(), e));
                }
              } else {
                return handleErrorResponse(response);
              }
            });
  }

  /**
   * Get list of available reports.
   *
   * <p>Returns the list of reports supported by the COUNTER API server for the customer, including
   * the first and last months for which usage data has been processed and is available. This
   * information is specific to the customer ID.
   *
   * @param customerId Customer ID (required)
   * @param platform Platform (optional)
   * @return Future with list of available reports
   */
  public Future<List<Report>> getReports(String customerId, String platform) {
    HttpRequest<Buffer> request =
        client.getAbs(baseUrl + "/reports").addQueryParam("customer_id", customerId);

    if (platform != null && !platform.isEmpty()) {
      request.addQueryParam("platform", platform);
    }

    auth.applyAuth(request);

    return request
        .send()
        .compose(
            response -> {
              int statusCode = response.statusCode();
              Buffer body = response.body();

              if (statusCode >= 200 && statusCode < 300) {
                try {
                  Report[] reports = MAPPER.readValue(body.getBytes(), Report[].class);
                  return Future.succeededFuture(List.of(reports));
                } catch (Exception e) {
                  return Future.failedFuture(
                      new Counter51ClientException(
                          "Failed to parse reports response: " + e.getMessage(), e));
                }
              } else {
                return handleErrorResponse(response);
              }
            });
  }

  protected <T> Future<T> makeRequest(
      String path,
      String customerId,
      String beginDate,
      String endDate,
      String platform,
      String attributesToShow,
      String includeParentDetails,
      Class<T> responseType) {

    HttpRequest<Buffer> request =
        client
            .getAbs(baseUrl + path)
            .addQueryParam("customer_id", customerId)
            .addQueryParam("begin_date", beginDate)
            .addQueryParam("end_date", endDate);

    if (platform != null && !platform.isEmpty()) {
      request.addQueryParam("platform", platform);
    }

    request.addQueryParam("attributes_to_show", attributesToShow);

    if (includeParentDetails != null) {
      request.addQueryParam("include_parent_details", includeParentDetails);
    }

    auth.applyAuth(request);

    return request.send().compose(response -> handleResponse(response, responseType));
  }

  /**
   * Handle HTTP response. Can be overridden by subclasses to customize behavior.
   *
   * @param response HTTP response
   * @param responseType Expected response type
   * @return Future with parsed response
   */
  protected <T> Future<T> handleResponse(HttpResponse<Buffer> response, Class<T> responseType) {
    int statusCode = response.statusCode();
    Buffer body = response.body();

    if (statusCode >= 200 && statusCode < 300) {
      return parseResponse(body, statusCode, responseType);
    } else {
      return handleErrorResponse(response);
    }
  }

  /**
   * Handle non-2xx responses. Can be overridden by subclasses.
   *
   * @param response HTTP error response
   * @return Failed future with appropriate exception
   */
  protected <T> Future<T> handleErrorResponse(HttpResponse<Buffer> response) {
    String bodyStr = response.body() != null ? response.body().toString() : "";
    return Future.failedFuture(
        new Counter51ClientException(
            "HTTP " + response.statusCode() + ": " + response.statusMessage(),
            response.statusCode(),
            bodyStr));
  }

  /**
   * Parse response buffer into the expected type. Can be overridden by subclasses to handle
   * malformed responses.
   *
   * @param buffer Response body
   * @param statusCode HTTP status code
   * @param responseType Expected type
   * @return Future with parsed response
   */
  protected <T> Future<T> parseResponse(Buffer buffer, int statusCode, Class<T> responseType) {
    try {
      T result = MAPPER.readValue(buffer.getBytes(), responseType);
      return Future.succeededFuture(result);
    } catch (Exception e) {
      return handleParseError(buffer, statusCode, responseType, e);
    }
  }

  /**
   * Handle parsing errors. Can be overridden by subclasses for custom error handling.
   *
   * @param buffer Response body that failed to parse
   * @param statusCode HTTP status code
   * @param responseType Expected response type
   * @param parseException The parsing exception
   * @return Failed future with appropriate exception
   */
  protected <T> Future<T> handleParseError(
      Buffer buffer, int statusCode, Class<T> responseType, Exception parseException) {
    return Future.failedFuture(
        new Counter51ClientException(
            "Failed to parse response: " + parseException.getMessage(), parseException));
  }

  /**
   * Close the underlying HTTP client. Only closes the WebClient if this instance created it (via
   * Vertx constructor). If a shared WebClient was passed in, this does nothing - caller manages
   * lifecycle.
   */
  @Override
  public void close() {
    if (ownsClient) {
      client.close();
    }
  }
}
