package org.olf.erm.usage.counter50.csv.cellprocessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.util.CsvContext;

public class ParseEnumType<T> extends CellProcessorAdaptor {

  private final Method fromValue;

  public ParseEnumType(Class<T> clazz) {
    try {
      this.fromValue = clazz.getMethod("fromValue", String.class);
    } catch (NoSuchMethodException e) {
      throw new ParseEnumTypeRuntimeException(e);
    }
  }

  @Override
  public <T> T execute(Object value, CsvContext context) {
    if (value == null) {
      return null;
    }

    try {
      return (T) fromValue.invoke(null, value);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new ParseEnumTypeRuntimeException(e);
    }
  }

  private static class ParseEnumTypeRuntimeException extends RuntimeException {

    public ParseEnumTypeRuntimeException(Throwable cause) {
      super(cause);
    }
  }
}
