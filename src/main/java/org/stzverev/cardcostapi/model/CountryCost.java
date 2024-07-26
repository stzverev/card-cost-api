package org.stzverev.cardcostapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.stzverev.cardcostapi.validator.constraint.IsoTwoConstraint;

public record CountryCost(
        @Schema(example = "US", description = "Country iso2 code") @NotNull @IsoTwoConstraint String country,
        @Schema(example = "5", description = "Clearing cost") long cost) {

}
