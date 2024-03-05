package com.test.aggregationservice.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(properties = "endpoints.backend-services=http://localhost:9999")
@AutoConfigureWireMock(port = 9999)
@AutoConfigureWebTestClient
class AggregationControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

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
        stubFor(
                get(urlPathEqualTo("/pricing"))
                        .withQueryParam("q", equalTo("AB"))
                        .willReturn(
                                okJson(
                                        """
                                                {
                                                  "AB": 81.03
                                                }
                                                """
                                )
                        )
        );
        stubFor(
                get(urlPathEqualTo("/track"))
                        .withQueryParam("q", equalTo("109347263"))
                        .willReturn(
                                okJson(
                                        """
                                                {
                                                  "109347263": "COLLECTING"
                                                }
                                                """
                                )
                        )
        );
        stubFor(
                get(urlPathEqualTo("/shipments"))
                        .withQueryParam("q", equalTo("109347264"))
                        .willReturn(
                                okJson(
                                        """
                                                {
                                                  "109347264": ["envelope", "box"]
                                                }
                                                """
                                )
                        )
        );
        webTestClient.get()
                .uri("/aggregation?track=109347263&shipments=109347264&pricing=AB")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.pricing.AB").isEqualTo(81.03)
                .jsonPath("$.shipments.109347264[0]").isEqualTo("envelope")
                .jsonPath("$.shipments.109347264[1]").isEqualTo("box")
                .jsonPath("$.track.109347263").isEqualTo("COLLECTING");
    }

}