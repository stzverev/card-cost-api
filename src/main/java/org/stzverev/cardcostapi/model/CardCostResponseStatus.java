package org.stzverev.cardcostapi.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record CardCostResponseStatus(Status status, String message, int errorCode, Map<String, String> errors) {

    public CardCostResponseStatus(Status status, String message, int errorCode) {
        this(status, message, errorCode, null);
    }

}
