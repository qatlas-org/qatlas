package org.qatlas.backend.validator;

import org.qatlas.backend.validator.annotation.DataExists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class DataExistsValidator
        implements ConstraintValidator<DataExists, Object> {

   @Autowired
   private EntityManager entityManager;

   private Logger logger = LoggerFactory.getLogger(DataExistsValidator.class);

   private DataExists dataExists;

   @Override
   public void initialize(final DataExists constraint) {
      dataExists = constraint;
   }

   public boolean isValid(Object obj, ConstraintValidatorContext context) {
      if(dataExists.jpaEntity() != null) {
         if(obj != null) {
            return entityManager.find(dataExists.jpaEntity(), obj) != null;
         } else {
            logger.error("Property value is NULL. Hence, DataExists constraint validation failed.");
            return false;
         }
      } else {
         logger.warn("jpEntity property is NULL. hence, the DataExists constraint validation is ignored.");
         return true;
      }
   }
}
