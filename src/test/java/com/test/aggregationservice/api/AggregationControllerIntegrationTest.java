package com.test.aggregationservice.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(
        properties = {
                "endpoints.backend-services=http://localhost:9999",
                "request.bulk-size=3"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock(port = 9999)
@AutoConfigureWebTestClient
class AggregationControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void initStubs() {
        stubFor(
                get(urlPathEqualTo("/pricing"))
                        .withQueryParam("q", equalTo("AB"))
                        .withQueryParam("q", equalTo("AC"))
                        .withQueryParam("q", equalTo("AD"))
                        .willReturn(
                                okJson(
                                        """
                                                {
                                                  "AB": 1.00,
                                                  "AC": 2.00,
                                                  "AD": 3.00
                                                }
                                                """
                                )
                        )
        );
        stubFor(
                get(urlPathEqualTo("/track"))
                        .withQueryParam("q", equalTo("109347263"))
                        .withQueryParam("q", equalTo("109347264"))
                        .withQueryParam("q", equalTo("109347265"))
                        .willReturn(
                                okJson(
                                        """
                                                {
                                                  "109347263": "COLLECTING",
                                                  "109347264": "DELIVERING",
                                                  "109347265": "IN_TRANSIT"
                                                }
                                                """
                                )
                        )
        );
        stubFor(
                get(urlPathEqualTo("/shipments"))
                        .withQueryParam("q", equalTo("109347266"))
                        .withQueryParam("q", equalTo("109347267"))
                        .withQueryParam("q", equalTo("109347268"))
                        .willReturn(
                                okJson(
                                        """
                                                {
                                                  "109347266": ["envelope", "box"],
                                                  "109347267": ["box"],
                                                  "109347268": ["pallet"]
                                                }
                                                """
                                )
                        )
        );
    }

    @Test
    void returnBadRequestWhenTheParametersAreNotProvided() {
        webTestClient.get()
                .uri("/aggregation")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("message").isEqualTo("provided parameters are not valid");
    }

    @Test
    void returnBadRequestWhenTheParametersAreNotValid() {
        webTestClient.get()
                .uri("/aggregation?track=a,1&shipments=a,1&pricing=1,a")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("message").isEqualTo("provided parameters are not valid");
    }

    @Test
    void returnSuccessfulResponse() {
        webTestClient.get()
                .uri("/aggregation?track=109347263,109347264,109347265&shipments=109347266,109347267,109347268&pricing=AB,AC,AD")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.pricing.AB").isEqualTo(1.00)
                .jsonPath("$.pricing.AC").isEqualTo(2.00)
                .jsonPath("$.pricing.AD").isEqualTo(3.00)
                .jsonPath("$.shipments.109347266[0]").isEqualTo("envelope")
                .jsonPath("$.shipments.109347266[1]").isEqualTo("box")
                .jsonPath("$.shipments.109347267[0]").isEqualTo("box")
                .jsonPath("$.shipments.109347268[0]").isEqualTo("pallet")
                .jsonPath("$.track.109347263").isEqualTo("COLLECTING")
                .jsonPath("$.track.109347264").isEqualTo("DELIVERING")
                .jsonPath("$.track.109347265").isEqualTo("IN_TRANSIT");
    }
}