package org.olf.erm.usage.counter50;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.openapitools.client.model.COUNTERDatabaseReport;
import org.openapitools.client.model.COUNTERItemIdentifiers;
import org.openapitools.client.model.COUNTERItemPerformance;
import org.openapitools.client.model.COUNTERItemPerformanceInstance;
import org.openapitools.client.model.COUNTERItemReport;
import org.openapitools.client.model.COUNTERPlatformReport;
import org.openapitools.client.model.COUNTERTitleReport;
import org.openapitools.client.model.SUSHIOrgIdentifiers;
import org.openapitools.client.model.SUSHIReportHeader;
import org.openapitools.client.model.SUSHIReportHeaderReportAttributes;
import org.openapitools.client.model.SUSHIReportHeaderReportFilters;

public class TestUtil {

  private TestUtil() {}

  public static <T> void sort(T clazz) {
    if (clazz instanceof COUNTERTitleReport) {
      sort((COUNTERTitleReport) clazz);
    }
    if (clazz instanceof COUNTERItemReport) {
      sort((COUNTERItemReport) clazz);
    }
    if (clazz instanceof COUNTERPlatformReport) {
      sort((COUNTERPlatformReport) clazz);
    }
    if (clazz instanceof COUNTERDatabaseReport) {
      sort((COUNTERDatabaseReport) clazz);
    }
  }

  public static void sort(COUNTERTitleReport tr) {
    sortHeader(tr.getReportHeader());
    tr.getReportItems()
        .sort(
            Comparator.comparing(
                ctu -> Objects.hash(ctu.getTitle(), ctu.getYOP(), ctu.getAccessType())));
    tr.getReportItems()
        .forEach(
            ctu -> {
              sortItemID(ctu.getItemID());
              sortPerformance(ctu.getPerformance());
            });
  }

  public static void sort(COUNTERPlatformReport pr) {
    sortHeader(pr.getReportHeader());
    pr.getReportItems()
        .sort(
            Comparator.comparing(
                cpu -> Objects.hash(cpu.getPlatform(), cpu.getAccessMethod(), cpu.getDataType())));
    pr.getReportItems().forEach(cpu -> sortPerformance(cpu.getPerformance()));
  }

  public static void sort(COUNTERItemReport ir) {
    sortHeader(ir.getReportHeader());
    ir.getReportItems()
        .sort(Comparator.comparing(ciu -> Objects.hash(ciu.getItem(), ciu.getYOP())));
    ir.getReportItems()
        .forEach(
            ciu -> {
              sortItemID(ciu.getItemID());
              sortPerformance(ciu.getPerformance());
            });
  }

  public static void sort(COUNTERDatabaseReport dr) {
    sortHeader(dr.getReportHeader());
    dr.getReportItems().sort(Comparator.comparing(cdu -> Objects.hash(cdu.getDatabase())));
    dr.getReportItems()
        .forEach(
            cdu -> {
              sortItemID(cdu.getItemID());
              sortPerformance(cdu.getPerformance());
            });
  }

  private static void sortHeader(SUSHIReportHeader header) {
    if (header.getReportAttributes() != null) {
      header
          .getReportAttributes()
          .sort(Comparator.comparing(SUSHIReportHeaderReportAttributes::getName));
    }
    if (header.getInstitutionID() != null) {
      header.getInstitutionID().sort(Comparator.comparing(SUSHIOrgIdentifiers::getType));
    }
    header.getReportFilters().sort(Comparator.comparing(SUSHIReportHeaderReportFilters::getName));
  }

  private static void sortItemID(List<COUNTERItemIdentifiers> cids) {
    if (cids != null) {
      cids.sort(Comparator.comparing(COUNTERItemIdentifiers::getType));
    }
  }

  private static void sortPerformance(List<COUNTERItemPerformance> cips) {
    cips.sort(Comparator.comparing(cip -> cip.getPeriod().getBeginDate()));
    cips.forEach(
        cip ->
            cip.getInstance()
                .sort(Comparator.comparing(COUNTERItemPerformanceInstance::getMetricType)));
  }
}
