package org.olf.erm.usage.counter50.merger;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.openapitools.counter50.model.SUSHIErrorModel;
import org.openapitools.counter50.model.SUSHIOrgIdentifiers;
import org.openapitools.counter50.model.SUSHIReportHeader;
import org.openapitools.counter50.model.SUSHIReportHeaderReportAttributes;
import org.openapitools.counter50.model.SUSHIReportHeaderReportFilters;

public abstract class ReportsMerger<T> {

  public abstract T merge(List<T> reports);

  protected SUSHIReportHeader mergeHeaders(List<SUSHIReportHeader> headers) {
    if (headers == null || headers.contains(null)) {
      throw new IllegalArgumentException("Provided header list or list element is null");
    }

    SUSHIReportHeader result = new SUSHIReportHeader();
    result.setReportName(headers.get(0).getReportName());
    result.setReportID(headers.get(0).getReportID());
    result.setRelease(headers.get(0).getRelease());
    result.setInstitutionName(headers.get(0).getInstitutionName());

    List<SUSHIOrgIdentifiers> institutionIds =
        headers.stream()
            .map(SUSHIReportHeader::getInstitutionID)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());
    result.setInstitutionID(institutionIds.isEmpty() ? null : institutionIds);

    result.setCreated(headers.get(0).getCreated());
    result.setCreatedBy(headers.get(0).getCreatedBy());
    result.setCustomerID(headers.get(0).getCustomerID());

    List<SUSHIReportHeaderReportAttributes> reportAttributes =
        headers.stream()
            .map(SUSHIReportHeader::getReportAttributes)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(distinctByNormalized(ReportsMerger::normalizeReportAttribute))
            .collect(Collectors.toList());
    result.setReportAttributes(reportAttributes.isEmpty() ? null : reportAttributes);

    result.setReportFilters(mergeReportFilters(headers));
    List<SUSHIErrorModel> exceptions =
        headers.stream()
            .map(SUSHIReportHeader::getExceptions)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());
    result.setExceptions(exceptions.isEmpty() ? null : exceptions);
    return result;
  }

  private List<SUSHIReportHeaderReportFilters> mergeReportFilters(List<SUSHIReportHeader> headers) {

    List<SUSHIReportHeaderReportFilters> filtersWithoutDates =
        headers.stream()
            .flatMap(h -> h.getReportFilters().stream())
            .filter(
                f ->
                    !(f.getName().equalsIgnoreCase("begin_date")
                        || f.getName().equalsIgnoreCase("end_date")))
            .filter(distinctByNormalized(ReportsMerger::normalizeReportFilter))
            .collect(Collectors.toList());

    List<LocalDate> allDates =
        headers.stream()
            .flatMap(h -> Counter5Utils.getLocalDateFromReportHeader(h).stream())
            .collect(Collectors.toList());
    if (allDates.isEmpty()) {
      return filtersWithoutDates;
    }
    LocalDate min = Collections.min(allDates);
    LocalDate max = Collections.max(allDates);
    SUSHIReportHeaderReportFilters beginFilter = new SUSHIReportHeaderReportFilters();
    beginFilter.setName("Begin_Date");
    beginFilter.setValue(min.toString());
    SUSHIReportHeaderReportFilters endFilter = new SUSHIReportHeaderReportFilters();
    endFilter.setName("End_Date");
    endFilter.setValue(max.toString());
    filtersWithoutDates.add(beginFilter);
    filtersWithoutDates.add(endFilter);
    return filtersWithoutDates;
  }

  private static <T> Predicate<T> distinctByNormalized(UnaryOperator<T> normalizer) {
    Set<T> seen = new LinkedHashSet<>();
    return t -> seen.add(normalizer.apply(t));
  }

  private static String normalizePipeDelimitedValue(String value) {
    if (value == null || !value.contains("|")) {
      return value;
    }
    String[] segments = value.split("\\|");
    Arrays.sort(segments);
    return String.join("|", segments);
  }

  private static SUSHIReportHeaderReportAttributes normalizeReportAttribute(
      SUSHIReportHeaderReportAttributes attr) {
    SUSHIReportHeaderReportAttributes normalized = new SUSHIReportHeaderReportAttributes();
    normalized.setName(attr.getName());
    normalized.setValue(normalizePipeDelimitedValue(attr.getValue()));
    return normalized;
  }

  private static SUSHIReportHeaderReportFilters normalizeReportFilter(
      SUSHIReportHeaderReportFilters filter) {
    SUSHIReportHeaderReportFilters normalized = new SUSHIReportHeaderReportFilters();
    normalized.setName(filter.getName());
    normalized.setValue(normalizePipeDelimitedValue(filter.getValue()));
    return normalized;
  }
}
