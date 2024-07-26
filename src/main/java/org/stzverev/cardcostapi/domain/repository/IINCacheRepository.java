package org.stzverev.cardcostapi.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.stzverev.cardcostapi.domain.entity.IINCacheEntity;
import reactor.core.publisher.Mono;

public interface IINCacheRepository extends ReactiveMongoRepository<IINCacheEntity, String> {

    Mono<IINCacheEntity> findByIIN(String iin);

}
