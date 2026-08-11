package org.olf.erm.usage.counter41;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.niso.schemas.counter.DateRange;
import org.niso.schemas.counter.Metric;
import org.niso.schemas.counter.Report;
import org.niso.schemas.counter.ReportItem;
import org.olf.erm.usage.counter41.Counter4Utils.ReportSplitException;

/**
 * {@link ReportSplitter} creates each month's report property by property, so a property that is
 * not passed on is silently dropped from every split report.
 *
 * <p>This test does not list the properties. It fills every writable property of the report, its
 * customer and its report items by reflection, then splits a report covering a single month, where
 * the result has to equal the source. That covers both ways of getting it wrong: a property the
 * splitter leaves out, and a property that a model regenerated from the COUNTER 4.1 schema gains,
 * which is covered from the moment it appears without anyone updating a list here.
 *
 * <p>Every property is given a value of its own, so passing on the wrong one is caught as well. A
 * property whose type {@link #sampleValueFor} cannot build fails the test rather than being
 * skipped.
 */
public class SplitPreservesReportTest {

  private static final LocalDate BEGIN_DATE = LocalDate.of(2020, 1, 1);
  private static final LocalDate END_DATE = LocalDate.of(2020, 1, 31);

  /** Holds the usage of the month and is therefore built by the test itself. */
  private static final String ITEM_PERFORMANCE = "itemPerformance";

  /**
   * Returns an instance of the given type with every writable property set, except the ones named,
   * which the caller sets itself.
   */
  private <T> T fullyPopulated(Class<T> type, String... propertiesToSetSeparately) {
    Set<String> skipped = Set.of(propertiesToSetSeparately);
    try {
      T instance = type.getDeclaredConstructor().newInstance();
      for (PropertyDescriptor property : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
        Method setter = property.getWriteMethod();
        if (setter == null || skipped.contains(property.getName())) {
          continue;
        }
        setter.invoke(
            instance, sampleValueFor(setter.getGenericParameterTypes()[0], property.getName()));
      }
      return instance;
    } catch (ReflectiveOperationException | IntrospectionException e) {
      throw new AssertionError("Cannot populate " + type.getSimpleName(), e);
    }
  }

  /**
   * Returns a value to set the given property to. Lists get a single element so that a list that is
   * not passed on differs from one that is.
   */
  private Object sampleValueFor(Type type, String propertyName)
      throws ReflectiveOperationException {
    if (type instanceof ParameterizedType listType) {
      Class<?> elementType = (Class<?>) listType.getActualTypeArguments()[0];
      return new ArrayList<>(List.of(newInstanceOf(elementType, propertyName)));
    }
    Class<?> valueType = (Class<?>) type;
    if (valueType == String.class) {
      return "value of " + propertyName;
    }
    if (valueType == ZonedDateTime.class) {
      return ZonedDateTime.parse("2020-02-01T00:00:00Z");
    }
    if (valueType.isEnum()) {
      return valueType.getEnumConstants()[0];
    }
    return newInstanceOf(valueType, propertyName);
  }

  private Object newInstanceOf(Class<?> type, String propertyName)
      throws ReflectiveOperationException {
    try {
      return type.getDeclaredConstructor().newInstance();
    } catch (NoSuchMethodException | InvocationTargetException e) {
      throw new AssertionError(
          "Property '%s' is of type %s, which this test cannot build. Extend sampleValueFor()."
              .formatted(propertyName, type.getSimpleName()),
          e);
    }
  }

  private Metric createMetricOfMonth() {
    DateRange period = new DateRange();
    period.setBegin(BEGIN_DATE);
    period.setEnd(END_DATE);
    Metric metric = new Metric();
    metric.setPeriod(period);
    return metric;
  }

  private Report createReportCoveringOneMonth() {
    ReportItem reportItem = fullyPopulated(ReportItem.class, ITEM_PERFORMANCE);
    reportItem.setItemPerformance(new ArrayList<>(List.of(createMetricOfMonth())));

    Report.Customer customer = fullyPopulated(Report.Customer.class, "reportItems");
    customer.setReportItems(new ArrayList<>(List.of(reportItem)));

    Report report = fullyPopulated(Report.class, "customer");
    report.setCustomer(new ArrayList<>(List.of(customer)));
    return report;
  }

  @Test
  public void testThatEverySourceReportPropertyIsPassedOn() throws ReportSplitException {
    Report report = createReportCoveringOneMonth();

    List<Report> splitReports = Counter4Utils.split(report);

    assertThat(splitReports).hasSize(1);
    assertThat(splitReports.get(0))
        .as(
            "a report covering one month has to survive splitting unchanged; a difference names"
                + " the property that ReportSplitter fails to pass on")
        .usingRecursiveComparison()
        .isEqualTo(report);
  }

  /**
   * Without this the test above could pass because a property was never populated rather than
   * because it was passed on.
   */
  @Test
  public void testThatEveryPropertyOfTheSplitModelsGetsPopulated() {
    List.of(Report.class, Report.Customer.class, ReportItem.class)
        .forEach(
            type ->
                assertThat(unsetPropertiesOf(fullyPopulated(type)))
                    .as("every writable property of %s has to be populated", type.getSimpleName())
                    .isEmpty());
  }

  private List<String> unsetPropertiesOf(Object instance) {
    List<String> unsetProperties = new ArrayList<>();
    try {
      for (PropertyDescriptor property :
          Introspector.getBeanInfo(instance.getClass()).getPropertyDescriptors()) {
        if (property.getWriteMethod() != null
            && property.getReadMethod() != null
            && property.getReadMethod().invoke(instance) == null) {
          unsetProperties.add(property.getName());
        }
      }
    } catch (ReflectiveOperationException | IntrospectionException e) {
      throw new AssertionError("Cannot read " + instance.getClass().getSimpleName(), e);
    }
    return unsetProperties;
  }
}
