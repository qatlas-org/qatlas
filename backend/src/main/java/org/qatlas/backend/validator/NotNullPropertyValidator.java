package org.qatlas.backend.validator;

import org.qatlas.backend.validator.annotation.NotNullProperty;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;

public class NotNullPropertyValidator
        implements ConstraintValidator<NotNullProperty, Object> {

   private NotNullProperty constraint;

   private static final Logger LOGGER = LoggerFactory.getLogger(NotNullPropertyValidator.class);

   @Override
   public void initialize(final NotNullProperty constraint) {
      this.constraint = constraint;
   }

   public boolean isValid(
           final Object obj,
           final ConstraintValidatorContext context) {
      String propertyName = constraint.propertyName();
      try {
         return StringUtils.isNotBlank(BeanUtils.getProperty(obj, propertyName));
      } catch (IllegalAccessException e) {
        LOGGER.error("Exception while validating @NotNull on property {} ", propertyName,e);
      } catch (InvocationTargetException e) {
         LOGGER.error("Exception while validating @NotNull on property {} ", propertyName,e);
      } catch (NoSuchMethodException e) {
         LOGGER.error("Exception while validating @NotNull on property {} ", propertyName,e);
      }
      return false;
   }
}
