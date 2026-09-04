package org.qatlas.backend.validator;

import org.qatlas.backend.validator.annotation.IpAddress;
import org.apache.commons.validator.routines.InetAddressValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IpAddressValidator
        implements ConstraintValidator<IpAddress, Object> {

    private IpAddress ipAddress;

    @Override
    public void initialize(final IpAddress constraintAnnotation) {
        ipAddress = constraintAnnotation;
    }

    @Override
    public boolean isValid(
            final Object value,
            final ConstraintValidatorContext context) {
        boolean nullable = ipAddress.nullable();
        if (nullable && value == null) {
            return true;
        } else if (!nullable && value == null) {
            return false;
        }
        String ipAddressInput = (String) value;
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(ipAddressInput)
            || validator.isValidInet6Address(ipAddressInput);
    }
}
