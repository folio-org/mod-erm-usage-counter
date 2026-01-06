package org.olf.erm.usage.counter50.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.counter50.model.COUNTERDatabaseReport;
import org.openapitools.counter50.model.COUNTERItemReport;
import org.openapitools.counter50.model.COUNTERPlatformReport;
import org.openapitools.counter50.model.COUNTERTitleReport;

@ExtendWith(VertxExtension.class)
@WireMockTest
@Timeout(5)
class Counter50ClientIT {

  private Counter50Client client;

  @AfterEach
  void tearDown() {
    if (client != null) {
      client.close();
    }
  }

  @Test
  void shouldCreateClientWithSharedWebClient(Vertx vertx, WireMockRuntimeInfo wmInfo) {
    WebClient sharedClient = WebClient.create(vertx);
    Counter50Auth auth = new Counter50Auth("apiKey", "requestorId");

    client = new Counter50Client(sharedClient, wmInfo.getHttpBaseUrl(), auth);

    assertThat(client).isNotNull();
    assertThat(client.client).isEqualTo(sharedClient);
    assertThat(client.ownsClient).isFalse();

    sharedClient.close();
  }

  @Test
  void shouldCreateClientWithOwnWebClient(Vertx vertx, WireMockRuntimeInfo wmInfo) {
    Counter50Auth auth = new Counter50Auth("apiKey", "requestorId");

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    assertThat(client).isNotNull();
    assertThat(client.client).isNotNull();
    assertThat(client.ownsClient).isTrue();
  }

  @Test
  void shouldCreateClientWithCustomOptions(Vertx vertx, WireMockRuntimeInfo wmInfo) {
    WebClientOptions options = new WebClientOptions().setFollowRedirects(false);
    Counter50Auth auth = new Counter50Auth("apiKey", "requestorId");

    client = new Counter50Client(vertx, options, wmInfo.getHttpBaseUrl(), auth);

    assertThat(client).isNotNull();
    assertThat(client.client).isNotNull();
    assertThat(client.ownsClient).isTrue();
  }

  @Test
  void shouldHandleNullAuth(Vertx vertx, WireMockRuntimeInfo wmInfo) {
    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), null);

    assertThat(client).isNotNull();
    assertThat(client.auth).isNotNull();
    assertThat(client.auth.getApiKey()).isNull();
    assertThat(client.auth.getRequestorId()).isNull();
  }

  @Test
  void shouldFetchTitleReport(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": {
            "Report_Name": "Title Master Report",
            "Report_ID": "TR"
          },
          "Report_Items": []
        }
        """;

    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam("begin_date", equalTo("2024-01-01"))
            .withQueryParam("end_date", equalTo("2024-12-31"))
            .withQueryParam(
                "attributes_to_show",
                equalTo("Data_Type|Section_Type|YOP|Access_Type|Access_Method"))
            .withQueryParam("include_parent_details", absent())
            .withQueryParam("api_key", equalTo("test-key"))
            .withQueryParam("requestor_id", equalTo("test-requestor"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter50Auth auth = new Counter50Auth("test-key", "test-requestor");
    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  assertThat(report).isNotNull();
                  assertThat(report).isInstanceOf(COUNTERTitleReport.class);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldFetchItemReport(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": {
            "Report_Name": "Item Master Report",
            "Report_ID": "IR"
          },
          "Report_Items": []
        }
        """;

    stubFor(
        get(urlPathEqualTo("/reports/ir"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam(
                "attributes_to_show",
                equalTo(
                    "Authors|Publication_Date|Article_Version|Data_Type|YOP|Access_Type|Access_Method"))
            .withQueryParam("include_parent_details", equalTo("True"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter50Auth auth = new Counter50Auth("test-key", "test-requestor");
    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReportsIR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  assertThat(report).isNotNull();
                  assertThat(report).isInstanceOf(COUNTERItemReport.class);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldFetchDatabaseReport(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": {
            "Report_Name": "Database Master Report",
            "Report_ID": "DR"
          },
          "Report_Items": []
        }
        """;

    stubFor(
        get(urlPathEqualTo("/reports/dr"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam("attributes_to_show", equalTo("Data_Type|Access_Method"))
            .withQueryParam("include_parent_details", absent())
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), new Counter50Auth(null, null));

    client
        .getReportsDR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  assertThat(report).isNotNull();
                  assertThat(report).isInstanceOf(COUNTERDatabaseReport.class);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldFetchPlatformReport(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": {
            "Report_Name": "Platform Master Report",
            "Report_ID": "PR"
          },
          "Report_Items": []
        }
        """;

    stubFor(
        get(urlPathEqualTo("/reports/pr"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam("attributes_to_show", equalTo("Data_Type|Access_Method"))
            .withQueryParam("include_parent_details", absent())
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), new Counter50Auth(null, null));

    client
        .getReportsPR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  assertThat(report).isNotNull();
                  assertThat(report).isInstanceOf(COUNTERPlatformReport.class);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleHttpError(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(get(urlPathEqualTo("/reports/tr")).willReturn(notFound().withBody("Not found")));

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), new Counter50Auth(null, null));

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter50ClientException.class);
                  Counter50ClientException ex = (Counter50ClientException) error;
                  assertThat(ex.getStatusCode()).isEqualTo(404);
                  assertThat(ex.getResponseBody()).contains("Not found");
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleServerError(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .willReturn(serverError().withBody("Internal server error")));

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), new Counter50Auth(null, null));

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter50ClientException.class);
                  Counter50ClientException ex = (Counter50ClientException) error;
                  assertThat(ex.getStatusCode()).isEqualTo(500);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleMalformedJson(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .willReturn(ok().withHeader("Content-Type", "application/json").withBody("not json")));

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), new Counter50Auth(null, null));

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter50ClientException.class);
                  assertThat(error.getMessage()).contains("Failed to parse response");
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldStripTrailingSlashFromBaseUrl(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .willReturn(ok().withBody("{\"Report_Header\":{\"Report_ID\":\"TR\"}}")));

    String urlWithSlash = wmInfo.getHttpBaseUrl() + "/";
    client = new Counter50Client(vertx, urlWithSlash, null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(getRequestedFor(urlPathEqualTo("/reports/tr")));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldIncludePlatformWhenProvided(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .withQueryParam("platform", equalTo("TestPlatform"))
            .willReturn(ok().withBody("{\"Report_Header\":{\"Report_ID\":\"TR\"}}")));

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", "TestPlatform")
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/reports/tr"))
                          .withQueryParam("platform", equalTo("TestPlatform")));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldNotIncludePlatformWhenNull(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .willReturn(ok().withBody("{\"Report_Header\":{\"Report_ID\":\"TR\"}}")));

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/reports/tr")).withoutQueryParam("platform"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldNotIncludePlatformWhenEmpty(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .willReturn(ok().withBody("{\"Report_Header\":{\"Report_ID\":\"TR\"}}")));

    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", "")
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/reports/tr")).withoutQueryParam("platform"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldNotSendAuthParametersWhenNull(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .willReturn(ok().withBody("{\"Report_Header\":{\"Report_ID\":\"TR\"}}")));

    Counter50Auth auth = new Counter50Auth(null, null);
    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/reports/tr"))
                          .withoutQueryParam("api_key")
                          .withoutQueryParam("requestor_id"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldNotSendAuthParametersWhenEmpty(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/reports/tr"))
            .willReturn(ok().withBody("{\"Report_Header\":{\"Report_ID\":\"TR\"}}")));

    Counter50Auth auth = new Counter50Auth("", "");
    client = new Counter50Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/reports/tr"))
                          .withoutQueryParam("api_key")
                          .withoutQueryParam("requestor_id"));
                  ctx.completeNow();
                }));
  }
}
