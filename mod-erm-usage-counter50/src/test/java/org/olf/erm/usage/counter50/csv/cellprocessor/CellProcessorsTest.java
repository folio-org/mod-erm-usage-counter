package org.olf.erm.usage.counter50.csv.cellprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.openapitools.client.model.COUNTERPublisherIdentifiers.TypeEnum.ISNI;
import static org.openapitools.client.model.COUNTERPublisherIdentifiers.TypeEnum.PROPRIETARY;

import org.junit.Test;
import org.olf.erm.usage.counter50.csv.cellprocessor.ParsePublisherID.PublisherIDParseException;
import org.openapitools.client.model.COUNTERItemContributors;
import org.openapitools.client.model.COUNTERItemContributors.TypeEnum;
import org.openapitools.client.model.COUNTERPublisherIdentifiers;

public class CellProcessorsTest {

  @Test
  public void testParseItemContributors() {
    assertThat(
            new ParseItemContributors(TypeEnum.AUTHOR)
                .execute("F Estelle (ORCID:0000-0001-2345-6789)", null))
        .asList()
        .containsExactly(
            new COUNTERItemContributors()
                .type(TypeEnum.AUTHOR)
                .name("F Estelle")
                .identifier("ORCID:0000-0001-2345-6789"));
    assertThat(
            new ParseItemContributors(TypeEnum.AUTHOR)
                .execute(
                    "F Estelle (ORCID:0000-0001-2345-6789); D Fred (ORCID:0000-0001-2345-6788)",
                    null))
        .asList()
        .containsExactlyInAnyOrder(
            new COUNTERItemContributors()
                .type(TypeEnum.AUTHOR)
                .name("F Estelle")
                .identifier("ORCID:0000-0001-2345-6789"),
            new COUNTERItemContributors()
                .type(TypeEnum.AUTHOR)
                .name("D Fred")
                .identifier("ORCID:0000-0001-2345-6788"));
    assertThat(new ParseItemContributors(TypeEnum.AUTHOR).execute(null, null)).isNull();
    assertThat(new ParseItemContributors(TypeEnum.AUTHOR).execute("F Estelle", null))
        .asList()
        .containsExactly(
            new COUNTERItemContributors().type(TypeEnum.AUTHOR).name("F Estelle").identifier(null));
    assertThat(new ParseItemContributors(TypeEnum.AUTHOR).execute("abc (def) abc", null))
        .asList()
        .containsExactly(
            new COUNTERItemContributors()
                .type(TypeEnum.AUTHOR)
                .name("abc (def) abc")
                .identifier(null));
  }

  @Test
  public void testParsePublisherID() {
    ParsePublisherID parsePublisherID = new ParsePublisherID();
    assertThatThrownBy(() -> parsePublisherID.execute("abc", null))
        .isInstanceOf(PublisherIDParseException.class);
    assertThat(parsePublisherID.execute(null, null)).asList().isEmpty();
    assertThat(parsePublisherID.execute("ISNI:1234; abc:123", null))
        .asList()
        .containsExactlyInAnyOrder(
            new COUNTERPublisherIdentifiers().type(ISNI).value("1234"),
            new COUNTERPublisherIdentifiers().type(PROPRIETARY).value("abc:123"));
  }
}
