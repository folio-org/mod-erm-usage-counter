package org.olf.erm.usage.counter50.csv.mapper.csv2report.merger;

import java.util.List;

public interface Merger<T> {

  List<T> mergeItems(List<T> items);

}
