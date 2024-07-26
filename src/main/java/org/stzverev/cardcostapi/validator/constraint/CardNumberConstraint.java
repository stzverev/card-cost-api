package org.stzverev.cardcostapi.validator.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.stzverev.cardcostapi.validator.CardNumberValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CardNumberValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CardNumberConstraint {

    String message() default "Invalid card number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
