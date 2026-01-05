package org.olf.erm.usage.counter51.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.counter51.model.DR;
import org.openapitools.counter51.model.IR;
import org.openapitools.counter51.model.PR;
import org.openapitools.counter51.model.Status;
import org.openapitools.counter51.model.TR;

@ExtendWith(VertxExtension.class)
@WireMockTest
@Timeout(5)
class Counter51ClientIT {

  private Counter51Client client;

  @AfterEach
  void tearDown() {
    if (client != null) {
      client.close();
    }
  }

  // Helper method to create valid report header JSON
  private static String validReportHeader(String reportName, String reportId) {
    return String.format(
        """
        {
          "Report_Name": "%s",
          "Report_ID": "%s",
          "Release": "5.1",
          "Institution_Name": "Test Institution",
          "Institution_ID": {
            "Proprietary": ["test:test-inst-1"]
          },
          "Report_Filters": {
            "Begin_Date": "2024-01-01",
            "End_Date": "2024-12-31"
          },
          "Created": "2024-01-01T00:00:00Z",
          "Created_By": "Test System",
          "Registry_Record": "https://registry.countermetrics.org/platform/00000000-0000-0000-0000-000000000000"
        }
        """,
        reportName, reportId);
  }

  @Test
  void shouldCreateClientWithSharedWebClient(Vertx vertx, WireMockRuntimeInfo wmInfo) {
    WebClient sharedClient = WebClient.create(vertx);
    Counter51Auth auth = new Counter51Auth("apiKey", "requestorId");

    client = new Counter51Client(sharedClient, wmInfo.getHttpBaseUrl(), auth);

    assertThat(client).isNotNull();
    assertThat(client.client).isEqualTo(sharedClient);
    assertThat(client.ownsClient).isFalse();

    sharedClient.close();
  }

  @Test
  void shouldCreateClientWithOwnWebClient(Vertx vertx, WireMockRuntimeInfo wmInfo) {
    Counter51Auth auth = new Counter51Auth("apiKey", "requestorId");

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    assertThat(client).isNotNull();
    assertThat(client.client).isNotNull();
    assertThat(client.ownsClient).isTrue();
  }

  @Test
  void shouldCreateClientWithCustomOptions(Vertx vertx, WireMockRuntimeInfo wmInfo) {
    WebClientOptions options = new WebClientOptions().setFollowRedirects(false);
    Counter51Auth auth = new Counter51Auth("apiKey", "requestorId");

    client = new Counter51Client(vertx, options, wmInfo.getHttpBaseUrl(), auth);

    assertThat(client).isNotNull();
    assertThat(client.client).isNotNull();
    assertThat(client.ownsClient).isTrue();
  }

  @Test
  void shouldHandleNullAuth(Vertx vertx, WireMockRuntimeInfo wmInfo) {
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

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
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Title Report", "TR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam("begin_date", equalTo("2024-01-01"))
            .withQueryParam("end_date", equalTo("2024-12-31"))
            .withQueryParam("attributes_to_show", equalTo("YOP|Access_Type|Access_Method"))
            .withQueryParam("include_parent_details", absent())
            .withQueryParam("api_key", equalTo("test-key"))
            .withQueryParam("requestor_id", equalTo("test-requestor"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter51Auth auth = new Counter51Auth("test-key", "test-requestor");
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  assertThat(report).isNotNull();
                  assertThat(report).isInstanceOf(TR.class);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldFetchItemReport(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Item Report", "IR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/ir"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam(
                "attributes_to_show",
                equalTo("Authors|Publication_Date|Article_Version|YOP|Access_Type|Access_Method"))
            .withQueryParam("include_parent_details", equalTo("True"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter51Auth auth = new Counter51Auth("test-key", "test-requestor");
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReportsIR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  assertThat(report).isNotNull();
                  assertThat(report).isInstanceOf(IR.class);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldFetchDatabaseReport(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Database Report", "DR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/dr"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam("attributes_to_show", equalTo("Access_Method"))
            .withQueryParam("include_parent_details", absent())
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsDR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  assertThat(report).isNotNull();
                  assertThat(report).isInstanceOf(DR.class);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldFetchPlatformReport(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Platform Report", "PR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/pr"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam("attributes_to_show", equalTo("Access_Method"))
            .withQueryParam("include_parent_details", absent())
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsPR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  assertThat(report).isNotNull();
                  assertThat(report).isInstanceOf(PR.class);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleHttpError(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(get(urlPathEqualTo("/r51/reports/tr")).willReturn(notFound().withBody("Not found")));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter51ClientException.class);
                  Counter51ClientException ex = (Counter51ClientException) error;
                  assertThat(ex.getStatusCode()).isEqualTo(404);
                  assertThat(ex.getResponseBody()).contains("Not found");
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleServerError(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .willReturn(serverError().withBody("Internal server error")));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter51ClientException.class);
                  Counter51ClientException ex = (Counter51ClientException) error;
                  assertThat(ex.getStatusCode()).isEqualTo(500);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleMalformedJson(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .willReturn(ok().withHeader("Content-Type", "application/json").withBody("not json")));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter51ClientException.class);
                  assertThat(error.getMessage()).contains("Failed to parse response");
                  ctx.completeNow();
                }));
  }

  // Provides test data for URL normalization tests
  private static Stream<Arguments> urlNormalizationTestCases() {
    return Stream.of(
        Arguments.of("single trailing slash", "/", "/r51/reports/tr"),
        Arguments.of("multiple trailing slashes", "///", "/r51/reports/tr"),
        Arguments.of("/r51 with trailing slash", "/r51/", "/r51/reports/tr"),
        Arguments.of("uppercase /R51", "/R51", "/R51/reports/tr"),
        Arguments.of("already ending with /r51", "/r51", "/r51/reports/tr"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("urlNormalizationTestCases")
  void shouldNormalizeBaseUrl(
      String testName,
      String urlSuffix,
      String expectedPath,
      Vertx vertx,
      VertxTestContext ctx,
      WireMockRuntimeInfo wmInfo) {

    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Title Report", "TR"));

    stubFor(
        get(urlPathEqualTo(expectedPath))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    String testUrl = wmInfo.getHttpBaseUrl() + urlSuffix;
    client = new Counter51Client(vertx, testUrl, null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(getRequestedFor(urlPathEqualTo(expectedPath)));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldIncludePlatformWhenProvided(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Title Report", "TR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .withQueryParam("platform", equalTo("TestPlatform"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", "TestPlatform")
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/reports/tr"))
                          .withQueryParam("platform", equalTo("TestPlatform")));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldNotIncludePlatformWhenNull(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Title Report", "TR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/reports/tr"))
                          .withoutQueryParam("platform"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldNotIncludePlatformWhenEmpty(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Title Report", "TR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", "")
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/reports/tr"))
                          .withoutQueryParam("platform"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldNotSendAuthParametersWhenNull(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Title Report", "TR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/reports/tr"))
                          .withoutQueryParam("api_key")
                          .withoutQueryParam("requestor_id"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldNotSendAuthParametersWhenEmpty(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Report_Header": %s,
          "Report_Items": []
        }
        """
            .formatted(validReportHeader("Title Report", "TR"));

    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter51Auth auth = new Counter51Auth("", "");
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.succeeding(
                report -> {
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/reports/tr"))
                          .withoutQueryParam("api_key")
                          .withoutQueryParam("requestor_id"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldGetStatus(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Description": "Test SUSHI API",
          "Service_Active": true
        }
        """;

    stubFor(
        get(urlPathEqualTo("/r51/status"))
            .withQueryParam("platform", equalTo("TestPlatform"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter51Auth auth = new Counter51Auth("test-key", "test-requestor");
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getStatus("TestPlatform")
        .onComplete(
            ctx.succeeding(
                status -> {
                  assertThat(status).isNotNull();
                  assertThat(status).isInstanceOf(Status.class);
                  // Verify NO auth parameters were sent (public endpoint)
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/status"))
                          .withoutQueryParam("api_key")
                          .withoutQueryParam("requestor_id"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldGetStatusWithoutPlatform(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        {
          "Description": "Test SUSHI API",
          "Service_Active": true
        }
        """;

    stubFor(
        get(urlPathEqualTo("/r51/status"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getStatus(null)
        .onComplete(
            ctx.succeeding(
                status -> {
                  assertThat(status).isNotNull();
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/status")).withoutQueryParam("platform"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleStatusError(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(get(urlPathEqualTo("/r51/status")).willReturn(notFound().withBody("Not found")));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getStatus(null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter51ClientException.class);
                  Counter51ClientException ex = (Counter51ClientException) error;
                  assertThat(ex.getStatusCode()).isEqualTo(404);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldGetMembers(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        [
          {
            "Customer_ID": "test-customer",
            "Institution_Name": "Test Institution",
            "Requestor_ID": "test-requestor"
          }
        ]
        """;

    stubFor(
        get(urlPathEqualTo("/r51/members"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam("platform", equalTo("TestPlatform"))
            .withQueryParam("api_key", equalTo("test-key"))
            .withQueryParam("requestor_id", equalTo("test-requestor"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter51Auth auth = new Counter51Auth("test-key", "test-requestor");
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getMembers("test-customer", "TestPlatform")
        .onComplete(
            ctx.succeeding(
                members -> {
                  assertThat(members).isNotNull();
                  assertThat(members).isInstanceOf(List.class);
                  assertThat(members).hasSize(1);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldGetMembersWithoutPlatform(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        [{
          "Customer_ID": "test-customer",
          "Institution_Name": "Test Institution"
        }]
        """;

    stubFor(
        get(urlPathEqualTo("/r51/members"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter51Auth auth = new Counter51Auth("test-key", "test-requestor");
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getMembers("test-customer", null)
        .onComplete(
            ctx.succeeding(
                members -> {
                  assertThat(members).isNotNull();
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/members"))
                          .withoutQueryParam("platform"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleMembersError(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/r51/members"))
            .willReturn(serverError().withBody("Internal server error")));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getMembers("test-customer", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter51ClientException.class);
                  Counter51ClientException ex = (Counter51ClientException) error;
                  assertThat(ex.getStatusCode()).isEqualTo(500);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldHandleMalformedMembersJson(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/r51/members"))
            .willReturn(ok().withHeader("Content-Type", "application/json").withBody("not json")));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getMembers("test-customer", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter51ClientException.class);
                  assertThat(error.getMessage()).contains("Failed to parse members response");
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldGetReports(Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        [
          {
            "Report_ID": "TR",
            "Report_Name": "Title Report",
            "Report_Description": "Full title-level usage statistics",
            "Path": "/reports/tr",
            "Release": "5.1",
            "First_Month_Available": "2024-01",
            "Last_Month_Available": "2024-12"
          },
          {
            "Report_ID": "DR",
            "Report_Name": "Database Report",
            "Report_Description": "Full database-level usage statistics",
            "Path": "/reports/dr",
            "Release": "5.1",
            "First_Month_Available": "2024-01",
            "Last_Month_Available": "2024-12"
          }
        ]
        """;

    stubFor(
        get(urlPathEqualTo("/r51/reports"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .withQueryParam("platform", equalTo("TestPlatform"))
            .withQueryParam("api_key", equalTo("test-key"))
            .withQueryParam("requestor_id", equalTo("test-requestor"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter51Auth auth = new Counter51Auth("test-key", "test-requestor");
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReports("test-customer", "TestPlatform")
        .onComplete(
            ctx.succeeding(
                reports -> {
                  assertThat(reports).isNotNull();
                  assertThat(reports).isInstanceOf(List.class);
                  assertThat(reports).hasSize(2);
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldGetReportsWithoutPlatform(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    String responseJson =
        """
        [{
          "Report_ID": "TR",
          "Report_Name": "Title Report",
          "Report_Description": "Full title-level usage statistics",
          "Release": "5.1",
          "First_Month_Available": "2024-01",
          "Last_Month_Available": "2024-12"
        }]
        """;

    stubFor(
        get(urlPathEqualTo("/r51/reports"))
            .withQueryParam("customer_id", equalTo("test-customer"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    Counter51Auth auth = new Counter51Auth("test-key", "test-requestor");
    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), auth);

    client
        .getReports("test-customer", null)
        .onComplete(
            ctx.succeeding(
                reports -> {
                  assertThat(reports).isNotNull();
                  verify(
                      getRequestedFor(urlPathEqualTo("/r51/reports"))
                          .withoutQueryParam("platform"));
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldFailOnMissingRequiredFields(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    // Response with missing required fields in Report_Header (missing Report_Name, Release,
    // Institution_Name, etc.)
    String responseJson =
        """
        {
          "Report_Header": {
            "Report_ID": "TR"
          },
          "Report_Items": []
        }
        """;

    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter51ClientException.class);
                  assertThat(error.getMessage()).contains("Failed to parse response");
                  assertThat(error.getCause()).isNotNull();
                  // Verify that the underlying cause is a validation-related exception
                  assertThat(error.getCause().getMessage()).contains("Validation failed");
                  ctx.completeNow();
                }));
  }

  @Test
  void shouldFailOnInvalidFieldValues(
      Vertx vertx, VertxTestContext ctx, WireMockRuntimeInfo wmInfo) {
    // Response with invalid Registry_Record (doesn't match URL pattern)
    String responseJson =
        """
        {
          "Report_Header": {
            "Report_Name": "Title Report",
            "Report_ID": "TR",
            "Release": "5.1",
            "Institution_Name": "Test Institution",
            "Institution_ID": {
              "Proprietary": ["test:test-inst-1"]
            },
            "Report_Filters": {
              "Begin_Date": "2024-01-01",
              "End_Date": "2024-12-31"
            },
            "Created": "2024-01-01T00:00:00Z",
            "Created_By": "Test System",
            "Registry_Record": "not-a-valid-url"
          },
          "Report_Items": []
        }
        """;

    stubFor(
        get(urlPathEqualTo("/r51/reports/tr"))
            .willReturn(
                ok().withHeader("Content-Type", "application/json").withBody(responseJson)));

    client = new Counter51Client(vertx, wmInfo.getHttpBaseUrl(), null);

    client
        .getReportsTR("test-customer", "2024-01-01", "2024-12-31", null)
        .onComplete(
            ctx.failing(
                error -> {
                  assertThat(error).isInstanceOf(Counter51ClientException.class);
                  assertThat(error.getMessage()).contains("Failed to parse response");
                  assertThat(error.getCause()).isNotNull();
                  // Verify validation failure for Registry_Record pattern constraint
                  assertThat(error.getCause().getMessage()).contains("Validation failed");
                  ctx.completeNow();
                }));
  }
}
