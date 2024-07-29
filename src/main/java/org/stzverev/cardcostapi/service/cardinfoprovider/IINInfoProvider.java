package org.stzverev.cardcostapi.service.cardinfoprovider;

import reactor.core.publisher.Mono;

/**
 * Represents an interface for providing card information based on the Issuer Identification Number (IIN).
 */
public interface IINInfoProvider {

    /**
     * Retrieves card information based on the Issuer Identification Number (IIN).
     *
     * @param iin The IIN to get card information for.
     * @return A Mono containing the card information.
     */
    Mono<IINInfo> getCardInfoByIin(String iin);

}
