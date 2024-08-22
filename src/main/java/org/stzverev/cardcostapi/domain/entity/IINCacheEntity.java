package org.stzverev.cardcostapi.domain.entity;

import lombok.Builder;

@Builder
public record IINCacheEntity(String iin, String issuingCountry) {
}
