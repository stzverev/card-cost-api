package org.stzverev.cardcostapi.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("iinCache")
@Data
@Builder
public class IINCacheEntity {

    @Id
    private String IIN;

    private String issuingCountry;

    @Indexed(name = "expireAt", expireAfterSeconds = 0)
    private Date expireAt;

}
