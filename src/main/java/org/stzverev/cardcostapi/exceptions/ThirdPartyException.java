package org.stzverev.cardcostapi.exceptions;

import lombok.Getter;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ThirdPartyException extends RuntimeException {

    private final HttpStatusCode code;

    private ThirdPartyException(final HttpStatusCode code, final FormattingTuple formattingTuple) {
        super(formattingTuple.getMessage(), formattingTuple.getThrowable());
        this.code = code;
    }

    public ThirdPartyException(final HttpStatusCode code, String message, Object... args) {
        this(code, MessageFormatter.arrayFormat(message, args));
    }

}
