package org.stzverev.cardcostapi.service.cardinfoprovider.binlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.stzverev.cardcostapi.configuration.properties.BinListConfig;
import org.stzverev.cardcostapi.exceptions.ThirdPartyException;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINExtractor;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfo;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfoProvider;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Represents an implementation of the {@link IINInfoProvider} interface that retrieves card information
 * from the Binlist provider.
 */
@RequiredArgsConstructor
@Slf4j
public class IINInfoProviderBinList implements IINInfoProvider {

    private final IINExtractor iinExtractor;

    private final ReactiveRedisOperations<String, Long> apiCallCounterRedisOperations;

    private final BinListConfig binListConfig;

    private static final String REDIS_COUNTER_KEY = "api-call-counter";

    /**
     * Retrieves card information based on the card number.
     *
     * @param cardNumber The card number.
     * @return A Mono containing the card information.
     * @throws IllegalArgumentException if the card number is null or empty, or if the card number is less than 6 characters.
     * @throws ThirdPartyException if there is an error retrieving the card information from the Binlist provider.
     */
    @Override
    public Mono<IINInfo> getCardInfoByNumber(final String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Card number cannot be null or empty"));
        }
        if (cardNumber.length() < 6) {
            return Mono.error(() -> new IllegalArgumentException("Card number must be at least 6 characters"));
        }
        final ReactiveValueOperations<String, Long> operations = apiCallCounterRedisOperations.opsForValue();
        final BinListConfig.MaxCallConfig maxCall = binListConfig.getMaxCall();
        return operations.setIfAbsent(REDIS_COUNTER_KEY, 0L, Duration.of(maxCall.getPeriod(),
                        maxCall.getTimeUnit().toChronoUnit()))
                .then(operations.increment(REDIS_COUNTER_KEY))
                .flatMap(count -> {
                    if (count > maxCall.getCount()) {
                        return Mono.error(new ThirdPartyException(HttpStatus.TOO_MANY_REQUESTS,
                                "Too many requests to binlist provider"));
                    }
                    return Mono.just(count);
                }).then(requestIinInfo(iinExtractor.getIin(cardNumber)));
    }

    private Mono<IINInfo> requestIinInfo(final String iin) {
        return WebClient.create(binListConfig.getBaseUrl())
                .get()
                .uri("/{cardNumber}", iin)
                .header("Accept-Version", "3")
                .retrieve()
                .onStatus(code -> code.is5xxServerError() || code.equals(HttpStatus.TOO_MANY_REQUESTS),
                        clientResponse -> Mono.error(
                                new ThirdPartyException(clientResponse.statusCode(),
                                        "Error getting card info by binlist provider")))
                .bodyToMono(BinlistResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)).jitter(0.75)
                        .filter(throwable -> throwable instanceof ThirdPartyException))
                .onErrorMap(throwable -> switch (throwable.getCause()) {
                    case ThirdPartyException cause -> cause;
                    default -> throwable;
                })
                .doOnNext(binlistResponse -> log.info("Card info is provided by binlist: {}", binlistResponse))
                .map(response -> new IINInfo(iin, response.country().alpha2()));
    }

}
