package org.qatlas.backend.validator.annotation;

import org.qatlas.backend.validator.DataExistsValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
@Constraint(validatedBy = DataExistsValidator.class)
public @interface DataExists {
    String message() default "{reporting.validation.constraints.DataExists.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<?> jpaEntity();
}
