package org.stzverev.cardcostapi.service.cardinfoprovider;

import org.springframework.stereotype.Component;

@Component
public class IINExtractor {

    public String getIin(String cardNumber) {
        if (cardNumber.length() >= 8) {
            return cardNumber.substring(0, 8);
        } else if (cardNumber.length() >= 6) {
            return cardNumber.substring(0, 6);
        }
        throw new IllegalArgumentException("Invalid card number: " + cardNumber);
    }

}
