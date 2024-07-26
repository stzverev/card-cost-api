package org.stzverev.cardcostapi.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.stzverev.cardcostapi.validator.constraint.CardNumberConstraint;

public class CardNumberValidator implements ConstraintValidator<CardNumberConstraint, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.matches("\\d{6,}");
    }

}
