package org.stzverev.cardcostapi.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.stzverev.cardcostapi.validator.constraint.IsoTwoConstraint;

public class IsoTwoValidator implements ConstraintValidator<IsoTwoConstraint, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return value.matches("^[A-Z]{2}$");
    }

}
