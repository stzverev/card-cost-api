package org.stzverev.cardcostapi.service.cardinfoprovider.cache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.stzverev.cardcostapi.domain.entity.IINCacheEntity;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINExtractor;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfo;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfoProvider;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * Represents a cache provider for retrieving card information based on the Issuer Identification Number (IIN).
 */
@RequiredArgsConstructor
@Slf4j
public class IINCacheProvider implements IINInfoProvider {

    private final IINInfoProvider iinInfoProvider;

    private static final String REDIS_PREFIX = "iin-cache-";

    private final IINExtractor iinExtractor;

    private final Duration expirationDuration;

    private final ReactiveRedisOperations<String, IINCacheEntity> redisOperationsIINCache;

    private final Sinks.Many<IINInfo> cachePublisher = Sinks.many().unicast().onBackpressureBuffer();

    @PostConstruct
    void init() {
        cachePublisher.asFlux()
                .flatMap(iinInfo ->
                        redisOperationsIINCache.opsForValue().set(REDIS_PREFIX + iinInfo.iin(),
                                        IINCacheEntity.builder()
                                                .iin(iinInfo.iin())
                                                .issuingCountry(iinInfo.country())
                                                .build(),
                                        expirationDuration)
                        .doOnNext(iinCacheEntity -> log.info("iin is saved to cache: {}", iinCacheEntity))
                        .onErrorContinue((throwable, o) -> log.error("Error saving IINCacheEntity. entity: {}", o,
                                throwable)))
                .subscribe();
    }

    /**
     * Retrieves card information based on the card number from cache.
     * If information is not found in cache, it will be fetched by delegate iinInfoProvider and
     * saved into cache
     *
     * @param cardNumber The card number.
     * @return A Mono containing the card information.
     */
    @Override
    public Mono<IINInfo> getCardInfoByNumber(final String cardNumber) {
        final String iin = iinExtractor.getIin(cardNumber);
        return redisOperationsIINCache.opsForValue().get(REDIS_PREFIX + iin)
                .doOnNext(iinCacheEntity -> log.info("iin is fetched from cache: {}", iinCacheEntity))
                .map(iinInfoProvider -> new IINInfo(iin, iinInfoProvider.issuingCountry()))
                .switchIfEmpty(iinInfoProvider.getCardInfoByNumber(cardNumber)
                        .doOnNext(cachePublisher::tryEmitNext));
    }

}
