package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.List;
import org.openapitools.client.model.COUNTERItemAttributes;
import org.openapitools.client.model.COUNTERItemContributors;
import org.openapitools.client.model.COUNTERItemContributors.TypeEnum;
import org.openapitools.client.model.COUNTERItemDates;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemParent;
import org.openapitools.client.model.COUNTERItemParent.DataTypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemParent extends CellProcessorAdaptor {

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }

    int colNumber = csvContext.getColumnNumber();
    List<Object> row = csvContext.getRowSource();

    COUNTERItemParent result = new COUNTERItemParent();
    result.setItemName((String) value);

    Object parentAuthor = row.get(colNumber);
    if (parentAuthor != null) {
      COUNTERItemContributors contributors = new COUNTERItemContributors();
      contributors.setType(TypeEnum.AUTHOR);
      contributors.setName((String) parentAuthor);
      result.addItemContributorsItem(contributors);
    }

    Object pubDate = row.get(colNumber + 1);
    if (pubDate != null) {
      COUNTERItemDates publicationDate = new COUNTERItemDates();
      publicationDate.setType(COUNTERItemDates.TypeEnum.PUBLICATION_DATE);
      publicationDate.setValue((String) pubDate);
      result.addItemDatesItem(publicationDate);
    }

    Object artV = row.get(colNumber + 2);
    if (artV != null) {
      COUNTERItemAttributes articleVersion = new COUNTERItemAttributes();
      articleVersion.setType(COUNTERItemAttributes.TypeEnum.ARTICLE_VERSION);
      articleVersion.setValue((String) artV);
      result.addItemAttributesItem(articleVersion);
    }

    Object dType = row.get(colNumber + 3);
    if (dType != null) {
      result.setDataType(DataTypeEnum.fromValue((String) dType));
    }

    Object doi = row.get(colNumber + 4);
    if (doi != null) {
      COUNTERItemIdentifiers doiID = new COUNTERItemIdentifiers();
      doiID.setType(COUNTERItemIdentifiers.TypeEnum.DOI);
      doiID.setValue((String) doi);
      result.addItemIDItem(doiID);
    }

    Object prop = row.get(colNumber + 5);
    if (prop != null) {
      COUNTERItemIdentifiers propID = new COUNTERItemIdentifiers();
      propID.setType(COUNTERItemIdentifiers.TypeEnum.PROPRIETARY);
      propID.setValue((String) prop);
      result.addItemIDItem(propID);
    }

    Object isbn = row.get(colNumber + 6);
    if (isbn != null) {
      COUNTERItemIdentifiers isbnID = new COUNTERItemIdentifiers();
      isbnID.setType(COUNTERItemIdentifiers.TypeEnum.ISBN);
      isbnID.setValue((String) isbn);
      result.addItemIDItem(isbnID);
    }

    Object printISSN = row.get(colNumber + 7);
    if (printISSN != null) {
      COUNTERItemIdentifiers issnID = new COUNTERItemIdentifiers();
      issnID.setType(COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN);
      issnID.setValue((String) printISSN);
      result.addItemIDItem(issnID);
    }

    Object onlineISSN = row.get(colNumber + 8);
    if (onlineISSN != null) {
      COUNTERItemIdentifiers onlineISSNID = new COUNTERItemIdentifiers();
      onlineISSNID.setType(COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN);
      onlineISSNID.setValue((String) onlineISSN);
      result.addItemIDItem(onlineISSNID);
    }

    Object uri = row.get(colNumber + 9);
    if (uri != null) {
      COUNTERItemIdentifiers uriID = new COUNTERItemIdentifiers();
      uriID.setType(COUNTERItemIdentifiers.TypeEnum.URI);
      uriID.setValue((String) uri);
      result.addItemIDItem(uriID);
    }

    return result;
  }
}
