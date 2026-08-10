package org.olf.erm.usage.counter50.internal;

import java.util.Objects;
import org.olf.erm.usage.counter50.Counter5Utils;

/** Internal helper for creating deep copies of COUNTER 5.0 report objects. */
public final class CloneUtils {

  private CloneUtils() {}

  /**
   * Creates a deep copy of the given object using {@link Counter5Utils#getDefaultObjectMapper()}.
   *
   * <p>The copy is made by a JSON round-trip, so only state that the mapper serializes is
   * reproduced, and shared references are not preserved: an object that {@code source} references
   * twice becomes two separate instances in the copy.
   *
   * @param source the object to copy
   * @param <T> the type of the object to copy
   * @return a deep copy of {@code source}
   * @throws NullPointerException if {@code source} is {@code null}
   * @throws IllegalArgumentException if {@code source} cannot be converted
   */
  public static <T> T deepCopy(T source) {
    Objects.requireNonNull(source, "source must not be null");
    @SuppressWarnings("unchecked")
    T copy = (T) Counter5Utils.getDefaultObjectMapper().convertValue(source, source.getClass());
    return copy;
  }
}
