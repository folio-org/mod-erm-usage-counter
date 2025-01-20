package org.olf.erm.usage.counter51;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractAuthors;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractExceptions;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractIdentifiers;
import static org.olf.erm.usage.counter51.ReportCsvFieldExtractor.extractUsageData;
import static org.olf.erm.usage.counter51.TestUtil.getObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportCsvFieldExtractorTest {

  @Test
  void testExtractUsageDataWithMissingMonth() {
    String data =
        """
        {
        "2022-01": 49,
        "2022-02": 90,
        "2022-03": 40,
        "2022-05": 86
        }
        """;

    List<String> expected = List.of("265", "49", "90", "40", "", "86");
    List<String> actual =
        extractUsageData(
            stringToJsonNode(data), List.of("2022-01", "2022-02", "2022-03", "2022-04", "2022-05"));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testExtractUsageDataWithMissingValues() {
    String data = "{}";

    List<String> expected = emptyList();
    List<String> actual = extractUsageData(stringToJsonNode(data), List.of("2022-01", "2022-02"));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testExtractAuthors() {
    String data =
        """
        [
          {
           "Name": "John Smith",
           "ISNI": "0000 0001 2134 568X",
           "ORCID": "0000-0001-2345-6789"
          },
          {
           "Name": "Jane Doe",
           "ORCID": "0000-0002-9876-5432"
          }
        ]
        """;

    String expected = "John Smith (ISNI:0000 0001 2134 568X); Jane Doe (ORCID:0000-0002-9876-5432)";
    String actual = extractAuthors(stringToJsonNode(data));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testExtractAuthorsWithMissingName() {
    String data =
        """
        [
          {
           "ORCID": "0000-0001-2345-6789"
          }
        ]
        """;

    String expected = "";
    String actual = extractAuthors(stringToJsonNode(data));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testExtractExceptions() {
    String data =
        """
        [
          {
            "Code": 3030,
            "Message": "No Usage Available for Requested Dates",
            "Help_URL": "http://example.com",
            "Data": "string"
          },
          {
            "Code": 3050,
            "Message": "Parameter Not Recognized in this Context",
            "Help_URL": "http://example.com",
            "Data": "string2"
          }
        ]
        """;

    String expected =
        "3030: No Usage Available for Requested Dates (string); "
            + "3050: Parameter Not Recognized in this Context (string2)";
    String actual = extractExceptions(stringToJsonNode(data));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testExtractExceptionsWithMissingData() {
    String data =
        """
        [
          {
            "Code": 3030,
            "Message": "No Usage Available for Requested Dates",
            "Help_URL": "http://example.com"
          },
          {
            "Code": 3050,
            "Message": "Parameter Not Recognized in this Context",
            "Help_URL": "http://example.com"
          }
        ]
        """;

    String expected =
        "3030: No Usage Available for Requested Dates; "
            + "3050: Parameter Not Recognized in this Context";
    String actual = extractExceptions(stringToJsonNode(data));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testExtractExceptionsWithMissingCodeOrMessage() {
    String data =
        """
        [
          {
            "Message": "No Usage Available for Requested Dates",
            "Help_URL": "http://example.com",
            "Data": "string"
          },
          {
            "Code": 3050,
            "Help_URL": "http://example.com",
            "Data": "string2"
          }
        ]
        """;

    String expected = "";
    String actual = extractExceptions(stringToJsonNode(data));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testExtractIdentifiers() {
    String data =
        """
        {
          "ISNI": [
            "4321432143214321",
            "1234123412341234"
          ],
          "ROR": [
            "04wx1wr19"
          ],
          "Proprietary": [
           "ebscohost:PubX"
          ]
        },
        """;

    String expected = "ISNI:4321432143214321; ROR:04wx1wr19; ebscohost:PubX";
    String actual = extractIdentifiers(stringToJsonNode(data));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testExtractIdentifiersWithEmptyArrayOrObjectNode() {
    String data = "{ \"ISNI\": [], \"ROR\": {} }";

    String expected = "";
    String actual = extractIdentifiers(stringToJsonNode(data));
    assertThat(actual).isEqualTo(expected);
  }

  private JsonNode stringToJsonNode(String s) {
    try {
      return getObjectMapper().readValue(s, JsonNode.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
