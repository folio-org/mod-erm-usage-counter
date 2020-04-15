package org.olf.erm.usage.counter50.csv.cellprocessor;

import org.openapitools.client.model.COUNTERTitleUsage.SectionTypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseSectionType extends CellProcessorAdaptor {

  @Override
  public SectionTypeEnum execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }
    return SectionTypeEnum.fromValue((String) value);
  }
}
