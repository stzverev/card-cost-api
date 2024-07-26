package org.stzverev.cardcostapi.service.cardinfoprovider.binlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.stzverev.cardcostapi.exceptions.ThirdPartyException;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfo;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfoProvider;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINExtractor;
import reactor.core.publisher.Mono;

/**
 * Represents an implementation of the {@link IINInfoProvider} interface that retrieves card information
 * from the Binlist provider.
 */
@RequiredArgsConstructor
@Slf4j
public class IINInfoProviderBinList implements IINInfoProvider {

    private final String binListBaseUrl;

    private final IINExtractor iinExtractor;

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
        return WebClient.create(binListBaseUrl)
                .get()
                .uri("/{cardNumber}", iinExtractor.getIin(cardNumber))
                .header("Accept-Version", "3")
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> Mono.error(
                                new ThirdPartyException(clientResponse.statusCode(),
                                        "Error getting card info by binlist provider")))
                .bodyToMono(BinlistResponse.class)
                .doOnNext(binlistResponse -> log.info("Card info is provided by binlist: {}", binlistResponse))
                .map(response -> new IINInfo(cardNumber, response.country().alpha2()));
    }

}
