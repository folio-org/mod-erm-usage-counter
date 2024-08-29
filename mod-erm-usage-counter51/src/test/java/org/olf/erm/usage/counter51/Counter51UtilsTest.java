package org.olf.erm.usage.counter51;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class Counter51UtilsTest {

  @Test
  void testCreateDefaultObjectMapper() {
    ObjectMapper expected = Mockito.mock(ObjectMapper.class);
    try (MockedStatic<ObjectMapperFactory> mockedStatic =
        Mockito.mockStatic(ObjectMapperFactory.class)) {
      mockedStatic.when(ObjectMapperFactory::createDefault).thenReturn(expected);
      assertThat(Counter51Utils.createDefaultObjectMapper()).isEqualTo(expected);
    }
  }
}
