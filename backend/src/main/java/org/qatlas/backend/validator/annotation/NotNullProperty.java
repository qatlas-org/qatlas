package org.qatlas.backend.validator.annotation;

import org.qatlas.backend.validator.NotNullPropertyValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotNullPropertyValidator.class)
@Documented
public @interface NotNullProperty {

    String message() default "ID property value is empty.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String propertyName();

}
