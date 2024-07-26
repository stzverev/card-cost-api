package org.stzverev.cardcostapi.exceptions;

import lombok.Getter;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

@Getter
public class CountryAlreadyExistException extends RuntimeException {

    private final String country;

    private CountryAlreadyExistException(final String country, final FormattingTuple formattingTuple) {
        super(formattingTuple.getMessage(), formattingTuple.getThrowable());
        this.country = country;
    }

    public CountryAlreadyExistException(final String country, String message, Object... args) {
        this(country, MessageFormatter.arrayFormat(message, args));
    }

}
