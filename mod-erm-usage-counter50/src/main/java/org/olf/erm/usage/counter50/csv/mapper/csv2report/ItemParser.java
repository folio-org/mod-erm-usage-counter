package org.olf.erm.usage.counter50.csv.mapper.csv2report;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanReader;
import org.supercsv.io.dozer.ICsvDozerBeanReader;
import org.supercsv.prefs.CsvPreference;

class ItemParser<T> {

  private static final Logger logger = LoggerFactory.getLogger(ItemParser.class);

  private final Class<T> clazz;

  public ItemParser(Class<T> clazz) {
    this.clazz = clazz;
  }

  public List<T> parseItems(
      String itemsString, String[] fieldMapping, CellProcessor[] processors, Class<?>[] hintTypes) {

    List<T> counterItemUsages = new ArrayList<>();
    try (ICsvDozerBeanReader beanReader =
        new CsvDozerBeanReader(new StringReader(itemsString), CsvPreference.STANDARD_PREFERENCE)) {

      beanReader.configureBeanMapping(clazz, fieldMapping, hintTypes);
      T item;
      while ((item = beanReader.read(clazz, processors)) != null) {
        counterItemUsages.add(item);
      }
    } catch (IOException e) {
      logger.error(String.format("Cannot map bean. %s", e.getCause()));
    }
    return counterItemUsages;
  }
}
