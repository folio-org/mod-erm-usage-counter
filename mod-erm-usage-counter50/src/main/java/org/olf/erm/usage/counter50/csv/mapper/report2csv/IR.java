package org.olf.erm.usage.counter50.csv.mapper.report2csv;

import static org.olf.erm.usage.counter50.csv.cellprocessor.IdentifierProcessor.getValue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.olf.erm.usage.counter50.csv.cellprocessor.PublisherIDProcessor;
import org.openapitools.client.model.COUNTERItemAttributes;
import org.openapitools.client.model.COUNTERItemContributors;
import org.openapitools.client.model.COUNTERItemContributors.TypeEnum;
import org.openapitools.client.model.COUNTERItemDates;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemParent;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERItemUsage;

@SuppressWarnings("java:S125")
public class IR extends AbstractIRMapper {

  public IR(COUNTERItemReport report) {
    super(report);
  }

  @Override
  String[] getHeader() {
    return new String[] {
      "Item",
      "Publisher",
      "Publisher_ID",
      "Platform",
      "Authors",
      "Publication_Date",
      "Article_Version",
      "DOI",
      "Proprietary_ID",
      "ISBN",
      "Print_ISSN",
      "Online_ISSN",
      "URI",
      "Parent_Title",
      "Parent_Authors",
      "Parent_Publication_Date",
      "Parent_Article_Version",
      "Parent_Data_Type",
      "Parent_DOI",
      "Parent_Proprietary_ID",
      "Parent_ISBN",
      "Parent_Print_ISSN",
      "Parent_Online_ISSN",
      "Parent_URI",
      //      "Component_Title",
      //      "Component_Authors",
      //      "Component_Publication_Date",
      //      "Component_Data_Type",
      //      "Component_DOI",
      //      "Component_Proprietary_DOI",
      //      "Component_ISBN",
      //      "Component_Print_ISSN",
      //      "Component_Online_ISSN",
      //      "Component_URI",
      "Data_Type",
      "YOP",
      "Access_Type",
      "Access_Method"
    };
  }

  @Override
  List<Object> getValues(COUNTERItemUsage iu) {
    return Arrays.asList(
        iu.getItem(),
        iu.getPublisher(),
        PublisherIDProcessor.getPublisherID(iu.getPublisherID()),
        iu.getPlatform(),
        getAuthors(iu.getItemContributors()),
        getPublicationDate(iu.getItemDates()),
        getArticleVersion(iu.getItemAttributes()),
        getValue(iu.getItemID(), COUNTERItemIdentifiers.TypeEnum.DOI),
        getValue(iu.getItemID(), COUNTERItemIdentifiers.TypeEnum.PROPRIETARY),
        getValue(iu.getItemID(), COUNTERItemIdentifiers.TypeEnum.ISBN),
        getValue(iu.getItemID(), COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN),
        getValue(iu.getItemID(), COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN),
        getValue(iu.getItemID(), COUNTERItemIdentifiers.TypeEnum.URI),
        getParentTitle(iu.getItemParent()),
        getParentAuthors(iu.getItemParent()),
        getParentPublicationDate(iu.getItemParent()),
        getParentArticleVersion(iu.getItemParent()),
        getParentDataType(iu.getItemParent()),
        getParentIdentifier(iu.getItemParent(), COUNTERItemIdentifiers.TypeEnum.DOI),
        getParentIdentifier(iu.getItemParent(), COUNTERItemIdentifiers.TypeEnum.PROPRIETARY),
        getParentIdentifier(iu.getItemParent(), COUNTERItemIdentifiers.TypeEnum.ISBN),
        getParentIdentifier(iu.getItemParent(), COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN),
        getParentIdentifier(iu.getItemParent(), COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN),
        getParentIdentifier(iu.getItemParent(), COUNTERItemIdentifiers.TypeEnum.URI),
        //        getComponentTitle(iu.getItemComponent()),
        //        getComponentAuthors(iu.getItemComponent()),
        //        getComponentPublicationDate(iu.getItemComponent()),
        //        getComponentDataType(iu.getItemComponent()),
        //        getComponentIdentifier(iu.getItemComponent(),
        // COUNTERItemIdentifiers.TypeEnum.DOI),
        //        getComponentIdentifier(iu.getItemComponent(),
        // COUNTERItemIdentifiers.TypeEnum.PROPRIETARY),
        //        getComponentIdentifier(iu.getItemComponent(),
        // COUNTERItemIdentifiers.TypeEnum.ISBN),
        //        getComponentIdentifier(iu.getItemComponent(),
        // COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN),
        //        getComponentIdentifier(iu.getItemComponent(),
        // COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN),
        //        getComponentIdentifier(iu.getItemComponent(),
        // COUNTERItemIdentifiers.TypeEnum.URI),
        iu.getDataType(),
        iu.getYOP(),
        iu.getAccessType(),
        iu.getAccessMethod());
  }

  private String getAuthors(List<COUNTERItemContributors> contributors) {
    return (contributors == null)
        ? null
        : contributors.stream()
            .filter(c -> c.getType() == TypeEnum.AUTHOR)
            .map(
                c -> {
                  if (c.getIdentifier() != null) {
                    return c.getName() + " (" + c.getIdentifier() + ")";
                  } else {
                    return c.getName();
                  }
                })
            .collect(Collectors.joining("; "));
  }

  private String getPublicationDate(List<COUNTERItemDates> dates) {
    return (dates == null)
        ? null
        : dates.stream()
            .filter(d -> d.getType() == COUNTERItemDates.TypeEnum.PUBLICATION_DATE)
            .map(COUNTERItemDates::getValue)
            .collect(Collectors.joining("; "));
  }

  private String getArticleVersion(List<COUNTERItemAttributes> attrs) {
    return (attrs == null)
        ? null
        : attrs.stream()
            .filter(Objects::nonNull)
            .filter(a -> a.getType() == COUNTERItemAttributes.TypeEnum.ARTICLE_VERSION)
            .map(COUNTERItemAttributes::getValue)
            .collect(Collectors.joining("; "));
  }

  private String getParentTitle(COUNTERItemParent parent) {
    return (parent == null) ? null : parent.getItemName();
  }

  private String getParentAuthors(COUNTERItemParent parent) {
    return (parent == null) ? null : getAuthors(parent.getItemContributors());
  }

  private String getParentDataType(COUNTERItemParent parent) {
    return (parent == null || parent.getDataType() == null)
        ? null
        : (parent.getDataType().getValue());
  }

  private String getParentPublicationDate(COUNTERItemParent parent) {
    return (parent == null) ? null : getPublicationDate(parent.getItemDates());
  }

  private String getParentArticleVersion(COUNTERItemParent parent) {
    return (parent == null) ? null : getArticleVersion(parent.getItemAttributes());
  }

  private String getParentIdentifier(
      COUNTERItemParent parent, COUNTERItemIdentifiers.TypeEnum identifier) {
    return (parent == null) ? null : getValue(parent.getItemID(), identifier);
  }

  /*private String getComponentTitle(List<COUNTERItemComponent> components) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(COUNTERItemComponent::getItemName)
        .collect(Collectors.joining("; "));
  }

  private String getComponentAuthors(List<COUNTERItemComponent> components) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(c -> getAuthors(c.getItemContributors()))
        .collect(Collectors.joining("; "));
  }

  private String getComponentPublicationDate(List<COUNTERItemComponent> components) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(c -> getPublicationDate(c.getItemDates()))
        .collect(Collectors.joining("; "));
  }

  private String getComponentDataType(List<COUNTERItemComponent> components) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(c -> c.getDataType().getValue())
        .collect(Collectors.joining("; "));
  }

  private String getComponentIdentifier(
      List<COUNTERItemComponent> components, COUNTERItemIdentifiers.TypeEnum identifier) {
    if (components == null || components.isEmpty()) {
      return null;
    }
    return components.stream()
        .filter(Objects::nonNull)
        .map(c -> getValue(c.getItemID(), identifier))
        .collect(Collectors.joining(", "));
  }*/
}
