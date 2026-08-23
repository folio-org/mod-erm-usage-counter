package org.olf.erm.usage.counter51;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.annotation.ElementType;
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
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.setDeserializerModifier(new ValidationBeanDeserializerModifier(createValidator()));

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(simpleModule);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  /**
   * Creates a {@link Validator} that checks each bean without following its {@code @Valid}
   * references. The deserializer validates every bean after constructing it, so referenced beans
   * have already been checked. Disabling cascades prevents each parent, especially the report root,
   * from validating the same object graph again.
   */
  static Validator createValidator() {
    try (ValidatorFactory validatorFactory =
        Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .traversableResolver(new NonCascadingTraversableResolver())
            .buildValidatorFactory()) {
      return validatorFactory.getValidator();
    }
  }

  private static class NonCascadingTraversableResolver implements TraversableResolver {

    @Override
    public boolean isReachable(
        Object traversableObject,
        Path.Node traversableProperty,
        Class<?> rootBeanType,
        Path pathToTraversableObject,
        ElementType elementType) {
      return true;
    }

    @Override
    public boolean isCascadable(
        Object traversableObject,
        Path.Node traversableProperty,
        Class<?> rootBeanType,
        Path pathToTraversableObject,
        ElementType elementType) {
      return false;
    }
  }
}
