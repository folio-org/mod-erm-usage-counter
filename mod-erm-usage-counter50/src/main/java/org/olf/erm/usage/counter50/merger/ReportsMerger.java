package org.olf.erm.usage.counter50.merger;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.openapitools.client.model.SUSHIOrgIdentifiers;
import org.openapitools.client.model.SUSHIReportHeader;
import org.openapitools.client.model.SUSHIReportHeaderReportAttributes;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

public abstract class ReportsMerger<T> {

  public abstract T merge(List<T> reports);

  protected SUSHIReportHeader mergeHeaders(List<SUSHIReportHeader> headers) {
    SUSHIReportHeader result = new SUSHIReportHeader();
    result.setReportName(headers.get(0).getReportName());
    result.setReportID(headers.get(0).getReportID());
    result.setRelease(headers.get(0).getRelease());
    result.setInstitutionName(headers.get(0).getInstitutionName());

    List<SUSHIOrgIdentifiers> institutionIds =
        headers.stream()
            .flatMap(h -> h.getInstitutionID().stream())
            .distinct()
            .collect(Collectors.toList());
    result.setInstitutionID(institutionIds);
    result.setCreated(headers.get(0).getCreated());
    result.setCreatedBy(headers.get(0).getCreatedBy());
    result.setCustomerID(headers.get(0).getCustomerID());

    List<SUSHIReportHeaderReportAttributes> reportAttributes =
        headers.stream()
            .flatMap(
                h ->
                    Optional.ofNullable(h.getReportAttributes())
                        .orElse(Collections.emptyList())
                        .stream())
            .distinct()
            .collect(Collectors.toList());
    result.setReportAttributes((reportAttributes.isEmpty()) ? null : reportAttributes);

    result.setReportFilters(mergeReportFilters(headers));
    result.setExceptions(
        headers.stream()
            .map(SUSHIReportHeader::getExceptions)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .distinct()
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toList(), list -> (list.isEmpty()) ? null : list)));
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
            .distinct()
            .collect(Collectors.toList());

    List<LocalDate> allDates =
        headers.stream()
            .flatMap(h -> Counter5Utils.getLocalDateFromReportHeader(h).stream())
            .collect(Collectors.toList());
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
}
