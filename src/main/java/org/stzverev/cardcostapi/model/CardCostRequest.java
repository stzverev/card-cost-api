package org.stzverev.cardcostapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.stzverev.cardcostapi.validator.constraint.CardNumberConstraint;

public record CardCostRequest(
        @Schema(description = "card number. Should  contain at least 6 digits") @JsonProperty("card_number")
        @NotNull @CardNumberConstraint String cardNumber) {
}
