package org.olf.erm.usage.counter51;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

class ObjectMapperFactory {

  private ObjectMapperFactory() {}

  /**
   * Creates a {@link ObjectMapper} instance that is configured with validation support for COUNTER
   * 5.1 report models.
   *
   * @return a configured {@link ObjectMapper} instance.
   */
  public static ObjectMapper createDefault() {
    Validator validator;
    try (ValidatorFactory validatorFactory =
        Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()) {
      validator = validatorFactory.getValidator();
    }

    SimpleModule simpleModule = new SimpleModule();
    simpleModule.setDeserializerModifier(new ValidationBeanDeserializerModifier(validator));

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(simpleModule);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }
}
