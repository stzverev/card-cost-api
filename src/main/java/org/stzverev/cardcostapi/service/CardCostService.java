package org.stzverev.cardcostapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.stzverev.cardcostapi.domain.entity.CurrencyCostEntity;
import org.stzverev.cardcostapi.domain.repository.CurrencyCostRepository;
import org.stzverev.cardcostapi.exceptions.SearchCountryIsNotFoundException;
import org.stzverev.cardcostapi.model.CardCostRequest;
import org.stzverev.cardcostapi.model.CardCostResponse;
import org.stzverev.cardcostapi.model.CountryCost;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfoProvider;
import org.stzverev.cardcostapi.exceptions.CountryAlreadyExistException;
import org.stzverev.cardcostapi.exceptions.CountryIsNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * This class is responsible for providing card cost information and updating clearing cost information
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardCostService {

    private final IINInfoProvider IINInfoProvider;

    private final CurrencyCostRepository costRepository;

    /**
     * Retrieves the cost of a card based on the provided card number.
     *
     * @param cardCostRequest the request containing the card number
     * @return a Mono object that emits the CardCostResponse once the card cost is retrieved
     */
    public Mono<CardCostResponse> getCardCost(CardCostRequest cardCostRequest) {
        return IINInfoProvider.getCardInfoByNumber(cardCostRequest.cardNumber())
                .doOnNext(iinInfo -> log.info("Card info provided: {}", iinInfo))
                .flatMap(IINInfo -> getClearCostByCountry(IINInfo.country())
                        .map(clearCost -> new CardCostResponse(IINInfo.country(), clearCost)));
    }

    /**
     * Retrieves all country costs.
     *
     * @return a Flux of CountryCost representing all country costs.
     */
    public Flux<CountryCost> getAllCosts() {
        return costRepository.findAll()
                .map(this::mapToCurrencyCost);
    }

    /**
     * Retrieves the clear cost by country.
     *
     * @param country the country for which to retrieve the clear cost
     * @return a Mono emitting the clear cost as a Long
     * @throws SearchCountryIsNotFoundException if the country cost is not found
     */
    private Mono<Long> getClearCostByCountry(final String country) {
        return costRepository.findByIssuingCountry(country)
                .doOnNext(currencyCostEntity -> log.info("Country is found: {}", country))
                .switchIfEmpty(costRepository.findByIssuingCountry("")
                        .doOnNext(currencyCostEntity -> log.info("""
                                Requested country is not found. Fetched settings for others country.
                                Requested country: {}""", country)))
                .switchIfEmpty(Mono.error(() -> new SearchCountryIsNotFoundException(country,
                        "There is no country cost: {}", country)))
                .map(CurrencyCostEntity::getCost);
    }

    private CountryCost mapToCurrencyCost(final CurrencyCostEntity costEntity) {
        return new CountryCost(costEntity.getIssuingCountry(), costEntity.getCost());
    }

    /**
     * Retrieves the cost of a country.
     *
     * @param country the name of the country to get the cost for
     * @return a Mono representing the cost of the country
     * @throws SearchCountryIsNotFoundException if the country is not found in the database
     */
    public Mono<CountryCost> getCostByCountry(final String country) {
        return costRepository.findByIssuingCountry(country)
                .map(this::mapToCurrencyCost)
                .switchIfEmpty(Mono.error(() -> new SearchCountryIsNotFoundException(country,
                        "Country is not found: {}", country)))
                .doOnError(error -> log.info("Error searching for country: {}", country, error));
    }

    /**
     * Adds the cost for a specific country.
     *
     * @param cc The CountryCost object representing the country and its associated cost.
     * @return A Mono<Void> indicating the completion of the operation.
     * @throws CountryAlreadyExistException if the country already has an associated cost.
     */
    public Mono<Void> addCountryCost(final CountryCost cc) {
        return costRepository.existsByIssuingCountry(cc.country())
                .flatMap(exists -> exists ? Mono.error(() -> new CountryAlreadyExistException(
                        cc.country(), "Error during adding a country: {}", cc.country())) : Mono.just(true))
                .doOnError(error -> log.info("Error during adding a country: {}", cc.country(), error))
                .then(Mono.just(cc)
                        .map(CardCostService::mapToCountryCostEntity)
                        .flatMap(costRepository::save))
                .then();
    }

    private static CurrencyCostEntity mapToCountryCostEntity(final CountryCost cc) {
        return CurrencyCostEntity.builder()
                .issuingCountry(cc.country())
                .cost(cc.cost())
                .build();
    }

    /**
     * Updates the cost of a country in the cost repository.
     *
     * @param cc The CountryCost object containing the country and cost to be updated.
     * @return A Mono representing the completion of the update operation.
     * @throws CountryIsNotFoundException if the country is not found in the cost repository.
     */
    public Mono<Void> updateCountryCost(final CountryCost cc) {
        return costRepository.existsByIssuingCountry(cc.country())
                .flatMap(exists -> !exists ? Mono.error(() -> new CountryIsNotFoundException(cc.country(),
                        "Error during updating a country cost: {}", cc.country())) : Mono.just(true))
                .doOnError(error -> log.info("Error during updating a country cost: {}", cc.country(), error))
                .flatMap(ignore -> costRepository.findByIssuingCountry(cc.country()))
                .flatMap(currencyCostEntity -> {
                    currencyCostEntity.setCost(cc.cost());
                    return costRepository.save(currencyCostEntity);
                }).then();
    }

    /**
     * Deletes the country cost based on the issuing country.
     *
     * @param country the issuing country of the country cost to be deleted
     * @return a Mono<Void> indicating the completion of the deletion process.
     * @throws CountryIsNotFoundException if the country cost does not exist
     */
    public Mono<Void> deleteCountryCost(final String country) {
        return costRepository.existsByIssuingCountry(country)
                .flatMap(exists -> !exists ? Mono.error(() -> new CountryIsNotFoundException(country,
                        "Error during deleting a country cost: {}", country)) : Mono.just(true))
                .doOnError(error -> log.info("Error during deleting a country cost: {}", country, error))
                .then(costRepository.deleteByIssuingCountry(country));
    }

    /**
     * Adds the costs for multiple countries.
     *
     * @param countryCost The list of country costs to add.
     * @return A Mono that represents the asynchronous completion of the operation.
     */
    public Mono<Void> addCountryCosts(final List<CountryCost> countryCost) {
        final Flux<CurrencyCostEntity> countryCostEntities = Flux.fromIterable(countryCost)
                .map(CardCostService::mapToCountryCostEntity);
        return costRepository.saveAll(countryCostEntities).then();
    }

}
