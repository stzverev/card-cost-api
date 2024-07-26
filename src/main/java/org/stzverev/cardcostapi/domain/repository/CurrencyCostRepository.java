package org.stzverev.cardcostapi.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.stzverev.cardcostapi.domain.entity.CurrencyCostEntity;
import reactor.core.publisher.Mono;

public interface CurrencyCostRepository extends ReactiveMongoRepository<CurrencyCostEntity, Long> {

    Mono<CurrencyCostEntity> findByIssuingCountry(String country);

    Mono<Boolean> existsByIssuingCountry(String country);

    Mono<Void> deleteByIssuingCountry(String issuingCountry);


}
