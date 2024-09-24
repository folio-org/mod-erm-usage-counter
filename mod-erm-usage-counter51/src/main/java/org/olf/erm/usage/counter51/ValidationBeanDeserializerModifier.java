package org.olf.erm.usage.counter51;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.Set;

class ValidationBeanDeserializerModifier extends BeanDeserializerModifier {

  public static final String VALIDATION_FAILED_MSG = "Validation failed for object: ";
  private final transient Validator validator;

  public ValidationBeanDeserializerModifier(Validator validator) {
    this.validator = validator;
  }

  @Override
  public JsonDeserializer<?> modifyDeserializer(
      DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
    if (deserializer instanceof BeanDeserializer beanDeserializer) {
      return new BeanDeserializerWithValidation(beanDeserializer, validator);
    }
    return deserializer;
  }

  public static class BeanDeserializerWithValidation extends BeanDeserializer {
    private final transient Validator validator;

    public BeanDeserializerWithValidation(BeanDeserializerBase src, Validator validator) {
      super(src);
      this.validator = validator;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      Object bean = super.deserialize(p, ctxt);

      // Validate the deserialized object
      Set<ConstraintViolation<Object>> violations = validator.validate(bean);
      if (!violations.isEmpty()) {
        StringBuilder sb = new StringBuilder(VALIDATION_FAILED_MSG);
        for (ConstraintViolation<Object> violation : violations) {
          sb.append(String.format("%s: %s; ", violation.getPropertyPath(), violation.getMessage()));
        }
        throw new JsonMappingException(p, sb.toString());
      }

      return bean;
    }
  }
}
