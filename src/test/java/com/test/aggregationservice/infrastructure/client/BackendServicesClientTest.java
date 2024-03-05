package com.test.aggregationservice.infrastructure.client;

import com.test.aggregationservice.infrastructure.config.BackendServicesRestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureWireMock(port = 9999)
@SpringBootTest(
        classes = {BackendServicesRestConfig.class, BackendServicesClient.class},
        properties = {
                "endpoints.backend-services=http://localhost:9999",
                "backend-services.shipments=shipments",
                "backend-services.track=track",
                "backend-services.pricing=pricing"
        }
)
class BackendServicesClientTest {

    private static final Set<String> PARAMETERS = Set.of("1", "2");

    @Autowired
    private BackendServicesClient client;

    @Test
    void getShipmentsResponseSuccessfully() {
        stubFor(
                get(urlPathEqualTo("/shipments"))
                        .withQueryParam("q", equalTo("1"))
                        .withQueryParam("q", equalTo("2"))
                        .willReturn(
                                okJson(readFile("mock/shipments_response.json"))
                        )
        );

        Mono<Map<String, List<String>>> result = client.getShipments(PARAMETERS);

        StepVerifier.create(result)
                .expectNext(
                        Map.of(
                                "1", List.of("pallet"),
                                "2", List.of("envelope", "box")
                        )
                )
                .verifyComplete();
    }

    @CsvSource(value = {
            "503",
            "500",
            "400",
            "401"
    })
    @ParameterizedTest
    void getShipmentsResponseReturnMapWithParametersWhenErrorOccurs(String statusCode) {
        stubFor(
                get(urlPathEqualTo("/shipments"))
                        .withQueryParam("q", equalTo("1"))
                        .withQueryParam("q", equalTo("2"))
                        .willReturn(
                                status(Integer.parseInt(statusCode))
                        )
        );

        Mono<Map<String, List<String>>> result = client.getShipments(PARAMETERS);

        StepVerifier.create(result)
                .expectNextMatches(
                        map ->
                                map.size() == 2 &&
                                        map.containsKey("1") &&
                                        map.containsKey("2") &&
                                        map.get("1") == null &&
                                        map.get("1") == null
                )
                .verifyComplete();
    }

    @Test
    void getTrackingResponseSuccessfully() {
        stubFor(
                get(urlPathEqualTo("/track"))
                        .withQueryParam("q", equalTo("1"))
                        .withQueryParam("q", equalTo("2"))
                        .willReturn(
                                okJson(readFile("mock/track_response.json"))
                        )
        );

        Mono<Map<String, String>> result = client.getTracking(PARAMETERS);

        StepVerifier.create(result)
                .expectNext(
                        Map.of(
                                "1", "COLLECTING",
                                "2", "DELIVERING"
                        )
                )
                .verifyComplete();
    }

    @CsvSource(value = {
            "503",
            "500",
            "400",
            "401"
    })
    @ParameterizedTest
    void getTrackingResponseReturnMapWithParametersWhenErrorOccurs(String statusCode) {
        stubFor(
                get(urlPathEqualTo("/track"))
                        .withQueryParam("q", equalTo("1"))
                        .withQueryParam("q", equalTo("2"))
                        .willReturn(
                                status(Integer.parseInt(statusCode))
                        )
        );

        Mono<Map<String, String>> result = client.getTracking(PARAMETERS);

        StepVerifier.create(result)
                .expectNextMatches(this::mapContainsParameters)
                .verifyComplete();
    }

    @Test
    void getPricingResponseSuccessfully() {
        stubFor(
                get(urlPathEqualTo("/pricing"))
                        .withQueryParam("q", equalTo("1"))
                        .withQueryParam("q", equalTo("2"))
                        .willReturn(
                                okJson(readFile("mock/pricing_response.json"))
                        )
        );

        Mono<Map<String, Double>> result = client.getPricing(PARAMETERS);

        StepVerifier.create(result)
                .expectNext(
                        Map.of(
                                "1", 81.03D,
                                "2", 39.67D
                        )
                )
                .verifyComplete();
    }

    @CsvSource(value = {
            "503",
            "500",
            "400",
            "401"
    })
    @ParameterizedTest
    void getPricingResponseReturnMapWithParametersWhenErrorOccurs(String statusCode) {
        stubFor(
                get(urlPathEqualTo("/pricing"))
                        .withQueryParam("q", equalTo("1"))
                        .withQueryParam("q", equalTo("2"))
                        .willReturn(
                                status(Integer.parseInt(statusCode))
                        )
        );

        Mono<Map<String, Double>> result = client.getPricing(PARAMETERS);

        StepVerifier.create(result)
                .expectNextMatches(this::mapContainsParameters)
                .verifyComplete();
    }

    public static String readFile(String resource) {
        try {
            return Files.readString(new ClassPathResource(resource).getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <E> boolean mapContainsParameters(Map<String, E> result) {
        return result.size() == PARAMETERS.size() &&
                PARAMETERS.stream().allMatch(result::containsKey) &&
                PARAMETERS.stream().map(result::get).allMatch(Objects::isNull);
    }

}