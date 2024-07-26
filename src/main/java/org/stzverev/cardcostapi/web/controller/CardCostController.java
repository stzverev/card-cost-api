package org.stzverev.cardcostapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.stzverev.cardcostapi.model.CardCostRequest;
import org.stzverev.cardcostapi.model.CardCostResponse;
import org.stzverev.cardcostapi.model.CardCostResponseStatus;
import org.stzverev.cardcostapi.model.CountryCost;
import org.stzverev.cardcostapi.service.CardCostService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/countryCost")
@RequiredArgsConstructor
public class CardCostController {

    private final CardCostService service;

    @Operation(summary = "Get clearing cost by card number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card cost successfully received"),
            @ApiResponse(responseCode = "400", description = "Invalid card",
                    content = @Content(schema = @Schema(implementation = CardCostResponseStatus.class))),
            @ApiResponse(responseCode = "404", description = "There is no cost information for card country",
                    content = @Content(schema = @Schema(implementation = CardCostResponseStatus.class)))
    })
    @PostMapping("/cardCost")
    public Mono<CardCostResponse> getCardCost(@RequestBody @Validated CardCostRequest request) {
        return service.getCardCost(request);
    }

    @Operation(summary = "Get clearing cost information for all configured countries")
    @ApiResponses({
            @ApiResponse(responseCode = "200")
    })
    @GetMapping("/all")
    public Flux<CountryCost> getAllCosts() {
        return service.getAllCosts();
    }

    @Operation(summary = "Get clearing cost information for provided country")
    @GetMapping
    public Mono<CountryCost> getCostByCountry(@Parameter(description = "ISO2 code of requested country")
                                                  @RequestParam("country") final String country) {
        return service.getCostByCountry(country);
    }

    @Operation(summary = """
            Add new clearing cost for a country. To add common clearing cost to not specified country
             use empty string as country value in request""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Country clearing cost is added"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = CardCostResponseStatus.class)))
    })
    @PutMapping
    public Mono<Void> addCountryCost(@RequestBody @Validated final CountryCost countryCost) {
        return service.addCountryCost(countryCost);
    }

    @PutMapping("/batch")
    public Mono<Void> addCountryCost(@RequestBody @Validated final List<CountryCost> countryCost) {
        return service.addCountryCosts(countryCost);
    }

    @Operation(summary = """
            Update clearing cost for a country. To add common clearing cost to not specified country
             use empty string as country value in request""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Country successfully updated"),
            @ApiResponse(responseCode = "422", description = "Country is not found",
                    content = @Content(schema = @Schema(implementation = CardCostResponseStatus.class)))
    })
    @PatchMapping
    public Mono<Void> updateCountryCost(@RequestBody @Validated final CountryCost countryCost) {
        return service.updateCountryCost(countryCost);
    }

    @Operation(summary = "Delete clearing cost for a country")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Country successfully deleted"),
            @ApiResponse(responseCode = "422", description = "Country is not found",
                    content = @Content(schema = @Schema(implementation =
                            CardCostResponseStatus.class)))
    })
    @DeleteMapping
    public Mono<Void> deleteCountryCost(@Parameter(description = "ISO 2 of deleting country")
                                            @RequestParam final String country) {
        return service.deleteCountryCost(country);
    }

}
