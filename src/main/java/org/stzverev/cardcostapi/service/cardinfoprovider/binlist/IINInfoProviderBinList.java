package org.stzverev.cardcostapi.service.cardinfoprovider.binlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.stzverev.cardcostapi.exceptions.ThirdPartyException;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfo;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfoProvider;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static java.util.Optional.ofNullable;

/**
 * Represents an implementation of the {@link IINInfoProvider} interface that retrieves card information
 * from the Binlist provider.
 */
@RequiredArgsConstructor
@Slf4j
public class IINInfoProviderBinList implements IINInfoProvider {

    private final String binListBaseUrl;

    /**
     * Retrieves card information based on the Issuer Identification Number (IIN).
     *
     * @param iin The IIN
     * @return A Mono containing the card information.
     * @throws IllegalArgumentException If the provided IIN is null or empty, or if it has less than 6 characters.
     * @throws ThirdPartyException If there is an error getting card information from the Binlist provider.
     */
    @Override
    public Mono<IINInfo> getCardInfoByIin(final String iin) {
        if (iin == null || iin.isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("IIN cannot be null or empty"));
        }
        if (iin.length() < 6) {
            return Mono.error(() -> new IllegalArgumentException("IIN must be at least 6 characters"));
        }
        return WebClient.create(binListBaseUrl)
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
                .filter(response -> ofNullable(response.country())
                        .map(Country::alpha2)
                        .orElse(null) != null)
                .doOnNext(binlistResponse -> log.info("Card info is provided by binlist: {}", binlistResponse))
                .map(response -> new IINInfo(iin, response.country().alpha2()));
    }

}
