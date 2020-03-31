package org.olf.erm.usage.counter50.merger;

import java.util.List;
import org.openapitools.client.model.COUNTERDatabaseReport;

public class DRReportsMerger extends ReportsMerger<COUNTERDatabaseReport> {
    @Override
    public COUNTERDatabaseReport merge(List<COUNTERDatabaseReport> reports) {
        /*List<SUSHIReportHeader> headers = reports.stream().map(r -> r.getReportHeader()).collect(Collectors.toList());
        SUSHIReportHeader mergedHeader = mergeHeaders(headers);

        List<COUNTERDatabaseUsage> mergedReportItems = reports.stream().flatMap(r -> r.getReportItems().stream()).collect(Collectors.toList());

        COUNTERDatabaseReport result = new COUNTERDatabaseReport();
        result.setReportHeader(mergedHeader);
        result.setReportItems(mergedReportItems);
        return result;*/


        // TODO specify testdata
        return null;
    }
}
