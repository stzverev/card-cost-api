package org.stzverev.cardcostapi.service.cardinfoprovider.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.stzverev.cardcostapi.domain.entity.IINCacheEntity;
import org.stzverev.cardcostapi.domain.repository.IINCacheRepository;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINExtractor;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfo;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfoProvider;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    private final IINExtractor iinExtractor;

    private final Duration expirationDuration;

    /**
     * Retrieves card information based on the card number from cache.
     * If information is not found in cache, it will be fetched be delegate iinInfoProvider and
     * saved into cache
     *
     * @param cardNumber The card number.
     * @return A Mono containing the card information.
     */
    @Override
    public Mono<IINInfo> getCardInfoByNumber(final String cardNumber) {
        final String iin = iinExtractor.getIin(cardNumber);
        return iinCacheRepository.findByIIN(iin)
                .doOnNext(iinCacheEntity -> log.info("iin is fetched from cache: {}", iinCacheEntity))
                .map(iinInfoProvider -> new IINInfo(cardNumber, iinInfoProvider.getIssuingCountry()))
                .switchIfEmpty(iinInfoProvider.getCardInfoByNumber(cardNumber))
                .doOnNext(IINInfo ->
                        iinCacheRepository.save(IINCacheEntity.builder()
                                        .IIN(iin)
                                        .issuingCountry(IINInfo.country())
                                        .expireAt(new Date(System.currentTimeMillis() + expirationDuration.toMillis()))
                                        .build()
                                )
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnNext(iinCacheEntity -> log.info("iin is saved to cache: {}", iinCacheEntity))
                                .onErrorContinue((throwable, o) -> log.error("Error saving IINCacheEntity", throwable))
                                .subscribe()
                );
    }
}
