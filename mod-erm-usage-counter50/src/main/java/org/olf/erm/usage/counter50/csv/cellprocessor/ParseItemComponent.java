package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.util.List;
import org.openapitools.client.model.COUNTERItemComponent;
import org.openapitools.client.model.COUNTERItemComponent.DataTypeEnum;
import org.openapitools.client.model.COUNTERItemContributors;
import org.openapitools.client.model.COUNTERItemContributors.TypeEnum;
import org.openapitools.client.model.COUNTERItemDates;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemComponent extends CellProcessorAdaptor {

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }

    COUNTERItemComponent result = new COUNTERItemComponent();
    result.setItemName((String) value);

    int colNumber = csvContext.getColumnNumber();
    List<Object> row = csvContext.getRowSource();

    Object authors = row.get(colNumber);
    if (authors != null) {
      COUNTERItemContributors itemAuthors = new COUNTERItemContributors();
      itemAuthors.setType(TypeEnum.AUTHOR);
      itemAuthors.setName((String) authors);
      result.addItemContributorsItem(itemAuthors);
    }

    Object pubDate = row.get(++colNumber);
    if (pubDate != null) {
      COUNTERItemDates itemPubDate = new COUNTERItemDates();
      itemPubDate.setType(COUNTERItemDates.TypeEnum.PUBLICATION_DATE);
      itemPubDate.setValue((String) pubDate);
      result.addItemDatesItem(itemPubDate);
    }

    Object dType = row.get(++colNumber);
    if (dType != null) {
      result.setDataType(DataTypeEnum.fromValue((String) dType));
    }

    Object doi = row.get(++colNumber);
    if (doi != null) {
      COUNTERItemIdentifiers doiID = new COUNTERItemIdentifiers();
      doiID.setType(COUNTERItemIdentifiers.TypeEnum.DOI);
      doiID.setValue((String) doi);
      result.addItemIDItem(doiID);
    }

    Object prop = row.get(++colNumber);
    if (prop != null) {
      COUNTERItemIdentifiers propID = new COUNTERItemIdentifiers();
      propID.setType(COUNTERItemIdentifiers.TypeEnum.PROPRIETARY);
      propID.setValue((String) prop);
      result.addItemIDItem(propID);
    }

    Object isbn = row.get(++colNumber);
    if (isbn != null) {
      COUNTERItemIdentifiers isbnID = new COUNTERItemIdentifiers();
      isbnID.setType(COUNTERItemIdentifiers.TypeEnum.ISBN);
      isbnID.setValue((String) isbn);
      result.addItemIDItem(isbnID);
    }

    Object printISSN = row.get(++colNumber);
    if (printISSN != null) {
      COUNTERItemIdentifiers issnID = new COUNTERItemIdentifiers();
      issnID.setType(COUNTERItemIdentifiers.TypeEnum.PRINT_ISSN);
      issnID.setValue((String) printISSN);
      result.addItemIDItem(issnID);
    }

    Object onlineISSN = row.get(++colNumber);
    if (onlineISSN != null) {
      COUNTERItemIdentifiers onlineISSNID = new COUNTERItemIdentifiers();
      onlineISSNID.setType(COUNTERItemIdentifiers.TypeEnum.ONLINE_ISSN);
      onlineISSNID.setValue((String) onlineISSN);
      result.addItemIDItem(onlineISSNID);
    }

    Object uri = row.get(++colNumber);
    if (uri != null) {
      COUNTERItemIdentifiers uriID = new COUNTERItemIdentifiers();
      uriID.setType(COUNTERItemIdentifiers.TypeEnum.URI);
      uriID.setValue((String) uri);
      result.addItemIDItem(uriID);
    }

    return result;
  }
}
