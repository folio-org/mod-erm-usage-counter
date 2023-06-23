package org.olf.erm.usage.counter41.csv.mapper;

import java.util.List;
import org.dozer.DozerBeanMapper;

public class DozerMappingUtil {

  public static DozerBeanMapper createDozerBeanMapper() {
    DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
    dozerBeanMapper.setMappingFiles(List.of("dozer-mapping.xml"));
    return dozerBeanMapper;
  }

  private DozerMappingUtil() {}
}
