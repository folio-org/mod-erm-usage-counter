package org.olf.erm.usage.counter50.splitter;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.openapitools.counter50.model.COUNTERDatabaseReport;
import org.openapitools.counter50.model.COUNTERDatabaseUsage;
import org.openapitools.counter50.model.COUNTERItemPerformance;
import org.openapitools.counter50.model.COUNTERItemPerformancePeriod;
import org.openapitools.counter50.model.COUNTERItemReport;
import org.openapitools.counter50.model.COUNTERItemUsage;
import org.openapitools.counter50.model.COUNTERPlatformReport;
import org.openapitools.counter50.model.COUNTERPlatformUsage;
import org.openapitools.counter50.model.COUNTERTitleReport;
import org.openapitools.counter50.model.COUNTERTitleUsage;
import org.openapitools.counter50.model.SUSHIReportHeader;
import org.openapitools.counter50.model.SUSHIReportHeaderReportFilters;

/**
 * The splitters build each month's header and report items property by property, so a property that
 * is not passed on is silently dropped from every split report.
 *
 * <p>These tests do not list the properties. They fill every writable property of the source object
 * by reflection and check that splitting passes each one on, which covers both ways of getting it
 * wrong: a splitter that leaves out a property the model has, and a property that a model
 * regenerated from the COUNTER specification gains, which is covered from the moment it appears
 * without anyone updating a list here.
 *
 * <p>Every property is given a value of its own, so passing on the wrong one is caught as well. A
 * property whose type {@link #sampleValueFor} cannot build fails the test rather than being
 * skipped.
 */
public class SplitPreservesReportItemTest {

  private static final String BEGIN_DATE = "2020-01-01";
  private static final String END_DATE = "2020-01-31";

  /** Holds the usage of the month and is therefore expected to differ. */
  private static final String PERFORMANCE = "performance";

  /** Narrowed to the month by the splitter and therefore expected to differ. */
  private static final String REPORT_FILTERS = "reportFilters";

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

  private SUSHIReportHeader createReportHeaderCoveringOneMonth() {
    SUSHIReportHeader reportHeader = fullyPopulated(SUSHIReportHeader.class, REPORT_FILTERS);
    List<SUSHIReportHeaderReportFilters> reportFilters = new ArrayList<>();
    reportFilters.add(new SUSHIReportHeaderReportFilters().name("Begin_Date").value(BEGIN_DATE));
    reportFilters.add(new SUSHIReportHeaderReportFilters().name("End_Date").value(END_DATE));
    return reportHeader.reportFilters(reportFilters);
  }

  private List<COUNTERItemPerformance> createPerformanceOfMonth() {
    COUNTERItemPerformance performance =
        new COUNTERItemPerformance()
            .period(new COUNTERItemPerformancePeriod().beginDate(BEGIN_DATE).endDate(END_DATE))
            .instance(new ArrayList<>());
    return new ArrayList<>(List.of(performance));
  }

  private <E> List<E> listOf(E element) {
    return new ArrayList<>(List.of(element));
  }

  private void assertThatReportItemIsPassedOn(Object splitReportItem, Object sourceReportItem) {
    assertThat(splitReportItem)
        .as("the splitter has to pass on every property of the report item")
        .usingRecursiveComparison()
        .ignoringFields(PERFORMANCE)
        .isEqualTo(sourceReportItem);
  }

  private void assertThatReportHeaderIsPassedOn(Object splitHeader, Object sourceHeader) {
    assertThat(splitHeader)
        .as("AbstractReportsSplitter.createHeaderForPeriod has to pass on every property")
        .usingRecursiveComparison()
        .ignoringFields(REPORT_FILTERS)
        .isEqualTo(sourceHeader);
  }

  @Test
  public void testThatReportHeaderIsPassedOnCompletely() {
    SUSHIReportHeader reportHeader = createReportHeaderCoveringOneMonth();
    COUNTERTitleUsage reportItem =
        fullyPopulated(COUNTERTitleUsage.class, PERFORMANCE)
            .performance(createPerformanceOfMonth());
    COUNTERTitleReport report =
        new COUNTERTitleReport().reportHeader(reportHeader).reportItems(listOf(reportItem));

    List<COUNTERTitleReport> splitReports = new TRReportsSplitter().split(report);

    assertThat(splitReports).hasSize(1);
    assertThatReportHeaderIsPassedOn(splitReports.get(0).getReportHeader(), reportHeader);
  }

  @Test
  public void testThatTitleUsageIsPassedOnCompletely() {
    COUNTERTitleUsage reportItem =
        fullyPopulated(COUNTERTitleUsage.class, PERFORMANCE)
            .performance(createPerformanceOfMonth());
    COUNTERTitleReport report =
        new COUNTERTitleReport()
            .reportHeader(createReportHeaderCoveringOneMonth())
            .reportItems(listOf(reportItem));

    List<COUNTERTitleReport> splitReports = new TRReportsSplitter().split(report);

    assertThat(splitReports).hasSize(1);
    assertThatReportItemIsPassedOn(splitReports.get(0).getReportItems().get(0), reportItem);
  }

  @Test
  public void testThatDatabaseUsageIsPassedOnCompletely() {
    COUNTERDatabaseUsage reportItem =
        fullyPopulated(COUNTERDatabaseUsage.class, PERFORMANCE)
            .performance(createPerformanceOfMonth());
    COUNTERDatabaseReport report =
        new COUNTERDatabaseReport()
            .reportHeader(createReportHeaderCoveringOneMonth())
            .reportItems(listOf(reportItem));

    List<COUNTERDatabaseReport> splitReports = new DRReportsSplitter().split(report);

    assertThat(splitReports).hasSize(1);
    assertThatReportItemIsPassedOn(splitReports.get(0).getReportItems().get(0), reportItem);
  }

  @Test
  public void testThatPlatformUsageIsPassedOnCompletely() {
    COUNTERPlatformUsage reportItem =
        fullyPopulated(COUNTERPlatformUsage.class, PERFORMANCE)
            .performance(createPerformanceOfMonth());
    COUNTERPlatformReport report =
        new COUNTERPlatformReport()
            .reportHeader(createReportHeaderCoveringOneMonth())
            .reportItems(listOf(reportItem));

    List<COUNTERPlatformReport> splitReports = new PRReportsSplitter().split(report);

    assertThat(splitReports).hasSize(1);
    assertThatReportItemIsPassedOn(splitReports.get(0).getReportItems().get(0), reportItem);
  }

  @Test
  public void testThatItemUsageIsPassedOnCompletely() {
    COUNTERItemUsage reportItem =
        fullyPopulated(COUNTERItemUsage.class, PERFORMANCE).performance(createPerformanceOfMonth());
    COUNTERItemReport report =
        new COUNTERItemReport()
            .reportHeader(createReportHeaderCoveringOneMonth())
            .reportItems(listOf(reportItem));

    List<COUNTERItemReport> splitReports = new IRReportsSplitter().split(report);

    assertThat(splitReports).hasSize(1);
    assertThatReportItemIsPassedOn(splitReports.get(0).getReportItems().get(0), reportItem);
  }

  /**
   * Without this the tests above could pass because a property was never populated rather than
   * because it was passed on.
   */
  @Test
  public void testThatEveryPropertyOfTheSplitModelsGetsPopulated() {
    List.of(
            SUSHIReportHeader.class,
            COUNTERTitleUsage.class,
            COUNTERDatabaseUsage.class,
            COUNTERPlatformUsage.class,
            COUNTERItemUsage.class)
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
