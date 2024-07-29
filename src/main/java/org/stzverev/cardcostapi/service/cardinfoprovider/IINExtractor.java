package org.stzverev.cardcostapi.service.cardinfoprovider;

import org.springframework.stereotype.Component;

@Component
public class IINExtractor {

    public String getIin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            throw new IllegalArgumentException("Invalid card number: " + cardNumber);
        }
        return cardNumber.substring(0, Math.min(cardNumber.length(), 8));
    }

}
