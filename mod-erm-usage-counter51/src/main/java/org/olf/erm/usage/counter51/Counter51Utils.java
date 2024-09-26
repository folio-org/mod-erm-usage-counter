package org.olf.erm.usage.counter51;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.olf.erm.usage.counter51.ReportMerger.MergerException;
import org.olf.erm.usage.counter51.ReportSplitter.SplitterException;

public class Counter51Utils {

  private static final ReportSplitter reportSplitter = new ReportSplitter();
  private static final ReportMerger reportMerger = new ReportMerger();

  private Counter51Utils() {}

  /**
   * Creates a {@link ObjectMapper} instance that is configured with validation support for COUNTER
   * 5.1 report models.
   *
   * <p>See {@link ObjectMapperFactory#createDefault()}.
   *
   * @return a configured {@link ObjectMapper} instance.
   */
  public static ObjectMapper createDefaultObjectMapper() {
    return ObjectMapperFactory.createDefault();
  }

  /**
   * Splits a COUNTER report into multiple COUNTER reports that each span a single month.
   *
   * <p>See {@link ReportSplitter#splitReport(ObjectNode)}
   *
   * @param report the COUNTER report that should be splitted.
   * @return list of single-month COUNTER reports.
   * @throws SplitterException if an error occurs during splitting.
   */
  public static List<ObjectNode> splitReport(ObjectNode report) {
    return reportSplitter.splitReport(report);
  }

  /**
   * Merges a list of COUNTER reports into a single merged COUNTER report where the merged COUNTER
   * report contains the combined data of all reports. Provided reports need to
   *
   * <ul>
   *   <li>span a single month only
   *   <li>have equal <em>Report_Header</em> attributes, except for <em>Created</em>,
   *       <em>Created_By</em>, <em>Report_Filters.Begin_Date</em> and
   *       <em>Report_Filters.End_Date</em>
   * </ul>
   *
   * See {@link ReportMerger#mergeReports(List)}
   *
   * @param reports the list of COUNTER reports to be merged.
   * @return a merged COUNTER report containing the combined data from all reports.
   * @throws MergerException if an error occurs during merging.
   */
  public static ObjectNode mergeReports(List<ObjectNode> reports) {
    return reportMerger.mergeReports(reports);
  }
}
