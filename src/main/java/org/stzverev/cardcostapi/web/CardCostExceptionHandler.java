package org.stzverev.cardcostapi.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.stzverev.cardcostapi.exceptions.CountryAlreadyExistException;
import org.stzverev.cardcostapi.exceptions.CountryIsNotFoundException;
import org.stzverev.cardcostapi.exceptions.SearchCountryIsNotFoundException;
import org.stzverev.cardcostapi.exceptions.ThirdPartyException;
import org.stzverev.cardcostapi.model.CardCostResponseStatus;
import org.stzverev.cardcostapi.model.Status;

import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Controller advice class that handles exceptions thrown during card cost requests processing.
 */
@Slf4j
@ControllerAdvice
public class CardCostExceptionHandler {

    @ExceptionHandler(CountryAlreadyExistException.class)
    public ResponseEntity<CardCostResponseStatus> handleCountryAlreadyExistException(CountryAlreadyExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new CardCostResponseStatus(Status.ERROR, "Country already exist: " + ex.getCountry(),
                        HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(CountryIsNotFoundException.class)
    public ResponseEntity<CardCostResponseStatus> handleCountryIsNotFoundException(CountryIsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                new CardCostResponseStatus(Status.ERROR, "Country is not found: " + ex.getCountry(),
                        HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    @ExceptionHandler(SearchCountryIsNotFoundException.class)
    public ResponseEntity<CardCostResponseStatus> handleCountryIsNotFoundException(SearchCountryIsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new CardCostResponseStatus(Status.ERROR, "Country is not found: " + ex.getCountry(),
                        HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<CardCostResponseStatus> handleMethodArgumentNotValidException(WebExchangeBindException ex) {
        return ResponseEntity.badRequest().body(CardCostResponseStatus.builder()
                .message("Validation failed")
                .errors(getErrors(ex))
                .status(Status.ERROR)
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CardCostResponseStatus> handleException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(CardCostResponseStatus.builder()
                .message("Unhandled exception")
                .status(Status.ERROR)
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CardCostResponseStatus> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(CardCostResponseStatus.builder()
                .status(Status.ERROR)
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(ThirdPartyException.class)
    public ResponseEntity<CardCostResponseStatus> handleThirdPartyException(ThirdPartyException ex) {
        return ResponseEntity.status(getCodeBasedOnThirdParty(ex)).body(
                new CardCostResponseStatus(Status.ERROR, ex.getMessage(),
                        ex.getCode().value()));
    }

    private static HttpStatusCode getCodeBasedOnThirdParty(final ThirdPartyException ex) {
        return switch (ex.getCode()) {
            case HttpStatus.TOO_MANY_REQUESTS -> HttpStatus.SERVICE_UNAVAILABLE;
            case HttpStatus.BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_GATEWAY;
        };
    }

    private static Map<String, String> getErrors(final WebExchangeBindException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fieldError -> ofNullable(fieldError.getDefaultMessage())
                                .orElse("")));
    }

}
