package org.stzverev.cardcostapi.web.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.stzverev.cardcostapi.BaseSpringBootTestContainersTest;
import org.stzverev.cardcostapi.model.CardCostResponse;
import org.stzverev.cardcostapi.model.CountryCost;
import org.stzverev.cardcostapi.util.resourcereader.ResourceReader;

import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;

class CardCostControllerIntegrationTest extends BaseSpringBootTestContainersTest {

    @InjectWireMock("card-service")
    @SuppressWarnings("unused")
    private WireMockServer wireMockServer;

    @Value("${app.thirdrpovider.binlist.baseUrl}")
    private String wireMockUrl;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private ResourceReader resourceReader;

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Testing CRUD operations")
    class RestSequenceTest {

        @Test
        @DisplayName("Add new country - success")
        @Order(1)
        void testAddCountryCost_shouldReturnOk() {
            webClient.put()
                    .uri("/countryCost")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(fromResource(
                            new ClassPathResource("card-cost-api/countryCost/put-us.json")))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Get all countries - success")
        @Order(2)
        void shouldReturnAllCountryCosts() {
            webClient.get()
                    .uri("/countryCost/all")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody().jsonPath("$").isNotEmpty();
        }

        @Test
        @DisplayName("Get country cost - success")
        @Order(3)
        void testGetCountryCost_shouldReturnUSCost() {
            final CountryCost expectedResponse = resourceReader.from("card-cost-api/countryCost/put-us.json")
                    .mapTo(CountryCost.class);

            webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/countryCost")
                            .queryParam("country", expectedResponse.country())
                            .build())
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.country").value(Matchers.equalTo(expectedResponse.country()))
                    .jsonPath("$.cost").value(equalTo(Long.valueOf(expectedResponse.cost()).intValue()));
        }

        @Test
        @DisplayName("Delete country cost - success")
        @Order(4)
        void testDeleteCountryCost_shouldReturnOk() {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder.path("countryCost")
                            .queryParam("country", "US").build())
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Get not presented country - bad request")
        @Order(5)
        void testGetCountryCost_shouldReturnNotFound() {
            final CountryCost expectedResponse = resourceReader.from("card-cost-api/countryCost/put-us.json")
                    .mapTo(CountryCost.class);

            webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/countryCost")
                            .queryParam("country", expectedResponse.country())
                            .build())
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.status").value(Matchers.equalTo("ERROR"))
                    .jsonPath("$.message").value(Matchers.equalTo("Country is not found: US"));
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Testing card cost flow")
    class CardCostFlowTest {

        @BeforeAll
        void setup() {
            webClient.put()
                    .uri("/countryCost")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(fromResource(
                            new ClassPathResource("card-cost-api/countryCost/put-us.json")))
                    .exchange()
                    .expectStatus().isOk();
        }

        @DisplayName("Get card cost based on card info from thirdparty provider")
        @Order(1)
        @Test
        void testGetCardCost_shouldReturnOk() throws URISyntaxException {
            //GIVEN
            wireMockServer.stubFor(get("/37828224")
                            .withHost(WireMock.urlMatching(new URI(wireMockUrl).getHost()).getPattern())
                    .withHeader("Accept-Version", WireMock.equalTo("3"))
                    .willReturn(WireMock.aResponse()
                            .withBody(resourceReader.from("mock-api/binlist/response-us.json").readAsBytes())
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
            var expectedResponse = resourceReader.from("card-cost-api/cardCost/card-cost-us-response.json")
                    .mapTo(CardCostResponse.class);

            webClient.post()
                    .uri("/countryCost/cardCost")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fromResource(resourceReader.from("card-cost-api/cardCost/card-cost-us-request.json")
                            .getResource()))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.country").value(equalTo(expectedResponse.country()))
                    .jsonPath("$.cost").value(equalTo(expectedResponse.cost().intValue()));

            wireMockServer.verify(1, WireMock.getRequestedFor(urlMatching("/37828224")));
        }

        @DisplayName("Should get card info from cahce without response to thirdparty provider")
        @Order(2)
        @Execution(ExecutionMode.CONCURRENT)
        @RepeatedTest(100)
        void testGetCardCost_shouldReceiveFromCache() {
            //GIVEN
            var expectedResponse = resourceReader.from("card-cost-api/cardCost/card-cost-us-response.json")
                    .mapTo(CardCostResponse.class);

            webClient.post()
                    .uri("/countryCost/cardCost")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fromResource(resourceReader.from("card-cost-api/cardCost/card-cost-us-request.json")
                            .getResource()))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.country").value(equalTo(expectedResponse.country()))
                    .jsonPath("$.cost").value(equalTo(expectedResponse.cost().intValue()));
            wireMockServer.verify(0, WireMock.getRequestedFor(urlMatching("/37828224")));
        }

        @AfterAll
        void teardown() {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder.path("/countryCost")
                            .queryParam("country", "US")
                            .build())
                    .exchange()
                    .expectStatus().isOk();
        }

    }

}