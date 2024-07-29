package org.stzverev.cardcostapi.service.cardinfoprovider.cache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.stzverev.cardcostapi.domain.entity.IINCacheEntity;
import org.stzverev.cardcostapi.domain.repository.IINCacheRepository;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfo;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfoProvider;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Date;

/**
 * Represents a cache provider for retrieving card information based on the Issuer Identification Number (IIN).
 */
@RequiredArgsConstructor
@Slf4j
public class IINCacheProvider implements IINInfoProvider {

    private final IINInfoProvider iinInfoProvider;

    private final IINCacheRepository iinCacheRepository;

    private final Duration expirationDuration;

    private final Sinks.Many<IINInfo> cachePublisher = Sinks.many().replay().all();

    @PostConstruct
    void init() {
        cachePublisher.asFlux()
                .flatMap(iinInfo -> iinCacheRepository.save(IINCacheEntity.builder()
                                .IIN(iinInfo.iin())
                                .issuingCountry(iinInfo.country())
                                .expireAt(new Date(System.currentTimeMillis() + expirationDuration.toMillis()))
                                .build())
                        .doOnNext(iinCacheEntity -> log.info("iin is saved to cache: {}", iinCacheEntity))
                        .onErrorContinue((throwable, o) -> log.error("Error saving IINCacheEntity. entity: {}", o,
                                throwable)))
                .subscribe();
    }

    /**
     * Retrieves card information based on the Issuer Identification Number (IIN).
     *
     * @param iin The IIN to get card information for.
     * @return A Mono containing the card information.
     */
    @Override
    public Mono<IINInfo> getCardInfoByIin(final String iin) {
        return iinCacheRepository.findByIIN(iin)
                .doOnNext(iinCacheEntity -> log.info("iin is fetched from cache: {}", iinCacheEntity))
                .map(iinInfoProvider -> new IINInfo(iin, iinInfoProvider.getIssuingCountry()))
                .switchIfEmpty(iinInfoProvider.getCardInfoByIin(iin)
                        .doOnNext(cachePublisher::tryEmitNext));
    }

}
