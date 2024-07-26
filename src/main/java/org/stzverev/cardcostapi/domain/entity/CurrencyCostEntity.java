package org.stzverev.cardcostapi.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document("currencyCost")
public class CurrencyCostEntity {

    @Id
    private String issuingCountry;

    private long cost;

}
