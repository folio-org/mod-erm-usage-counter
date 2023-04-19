package org.olf.erm.usage.counter50.csv.cellprocessor;

import com.google.common.base.Splitter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openapitools.client.model.COUNTERItemContributors;
import org.openapitools.client.model.COUNTERItemContributors.TypeEnum;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseItemContributors extends CellProcessorAdaptor {

  private static final Pattern PATTERN = Pattern.compile("^(.*?)(?: \\((.*)\\))?$");
  private final TypeEnum type;

  public ParseItemContributors(TypeEnum type) {
    this.type = type;
  }

  @Override
  public Object execute(Object value, CsvContext csvContext) {
    if (value == null) {
      return null;
    }

    return Splitter.on(";")
        .trimResults()
        .splitToStream(String.valueOf(value))
        .map(
            authorString -> {
              Matcher matcher = PATTERN.matcher(authorString);
              matcher.matches();
              return new COUNTERItemContributors()
                  .type(type)
                  .name(matcher.group(1))
                  .identifier(matcher.group(2));
            })
        .collect(Collectors.toList());
  }
}
