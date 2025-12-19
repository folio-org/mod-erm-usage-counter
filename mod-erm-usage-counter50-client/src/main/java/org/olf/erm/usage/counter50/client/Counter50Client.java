package org.olf.erm.usage.counter50.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.openapitools.counter50.model.COUNTERDatabaseReport;
import org.openapitools.counter50.model.COUNTERItemReport;
import org.openapitools.counter50.model.COUNTERPlatformReport;
import org.openapitools.counter50.model.COUNTERTitleReport;

/**
 * Vert.x HTTP client for COUNTER 5.0 SUSHI API. Provides Future-based async API for fetching
 * COUNTER reports.
 */
public class Counter50Client implements AutoCloseable {

  private static final ObjectMapper MAPPER = Counter5Utils.getDefaultObjectMapper();

  protected final WebClient client;
  protected final boolean ownsClient;
  protected final String baseUrl;
  protected final Counter50Auth auth;

  /**
   * Primary constructor - all initialization happens here.
   *
   * @param client WebClient instance
   * @param baseUrl Base URL of SUSHI service
   * @param auth Authentication credentials
   * @param ownsClient Whether this instance owns the WebClient (and should close it)
   */
  private Counter50Client(
      WebClient client, String baseUrl, Counter50Auth auth, boolean ownsClient) {
    this.client = client;
    this.ownsClient = ownsClient;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.auth = auth != null ? auth : new Counter50Auth(null, null);
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
  public Counter50Client(WebClient client, String baseUrl, Counter50Auth auth) {
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
  public Counter50Client(Vertx vertx, String baseUrl, Counter50Auth auth) {
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
  public Counter50Client(
      Vertx vertx, WebClientOptions options, String baseUrl, Counter50Auth auth) {
    this(WebClient.create(vertx, options), baseUrl, auth, true);
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
  public Future<COUNTERTitleReport> getReportsTR(
      String customerId, String beginDate, String endDate, String platform) {
    return makeRequest(
        "/reports/tr",
        customerId,
        beginDate,
        endDate,
        platform,
        "Data_Type|Section_Type|YOP|Access_Type|Access_Method",
        null,
        COUNTERTitleReport.class);
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
  public Future<COUNTERItemReport> getReportsIR(
      String customerId, String beginDate, String endDate, String platform) {
    return makeRequest(
        "/reports/ir",
        customerId,
        beginDate,
        endDate,
        platform,
        "Authors|Publication_Date|Article_Version|Data_Type|YOP|Access_Type|Access_Method",
        "True",
        COUNTERItemReport.class);
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
  public Future<COUNTERDatabaseReport> getReportsDR(
      String customerId, String beginDate, String endDate, String platform) {
    return makeRequest(
        "/reports/dr",
        customerId,
        beginDate,
        endDate,
        platform,
        "Data_Type|Access_Method",
        null,
        COUNTERDatabaseReport.class);
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
  public Future<COUNTERPlatformReport> getReportsPR(
      String customerId, String beginDate, String endDate, String platform) {
    return makeRequest(
        "/reports/pr",
        customerId,
        beginDate,
        endDate,
        platform,
        "Data_Type|Access_Method",
        null,
        COUNTERPlatformReport.class);
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
        new Counter50ClientException(
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
        new Counter50ClientException(
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
