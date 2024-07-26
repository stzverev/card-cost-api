package org.stzverev.cardcostapi.service.cardinfoprovider;

import reactor.core.publisher.Mono;

/**
 * Represents an interface for providing card information based on the Issuer Identification Number (IIN).
 */
public interface IINInfoProvider {

    /**
     * Retrieves card information based on the card number.
     *
     * @param cardNumber The card number.
     * @return A Mono containing the card information.
     */
    Mono<IINInfo> getCardInfoByNumber(String cardNumber);

}
