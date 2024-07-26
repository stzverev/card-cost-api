package org.stzverev.cardcostapi.validator.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.stzverev.cardcostapi.validator.IsoTwoValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsoTwoValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IsoTwoConstraint {

    String message() default "Invalid country iso 2 code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
