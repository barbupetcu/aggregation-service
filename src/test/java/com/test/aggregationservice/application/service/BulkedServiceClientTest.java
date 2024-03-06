package com.test.aggregationservice.application.service;

import com.test.aggregationservice.infrastructure.client.BackendServicesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BulkedServiceClientTest {

    private BulkedServiceClient bulkedServiceClient;
    @Mock
    private BackendServicesClient backendServicesClient;

    @BeforeEach
    void init() {
        bulkedServiceClient = new BulkedServiceClient(backendServicesClient,3);
        bulkedServiceClient.init();
    }

    @Test
    void doNotPublishEventIfCapIsNotReachedForPricing() {
        Mono<Map<String, Double>> result = bulkedServiceClient.getPricing(Set.of("1", "2"));

        StepVerifier.create(result)
                .expectTimeout(Duration.ofSeconds(1));

        verifyNoInteractions(backendServicesClient);

    }

    @Test
    void forwardRequestWhenCapIsReachedForPricing() {
        doReturn(Mono.just(Map.of("1", 1.00, "2", 2.00, "3", 3.00)))
                .when(backendServicesClient).getPricing(Set.of("1", "2", "3"));

        Mono<Map<String, Double>> result1 = bulkedServiceClient.getPricing(Set.of("1", "2"));
        Mono<Map<String, Double>> result2 = bulkedServiceClient.getPricing(Set.of("3"));

        StepVerifier.create(Flux.merge(result1, result2))
                .expectNext(Map.of("1", 1.00, "2", 2.00))
                .expectNext(Map.of("3", 3.00))
                .verifyComplete();

    }

    @Test
    void doNotPublishEventIfCapIsNotReachedForTracking() {
        Mono<Map<String, String >> result = bulkedServiceClient.getTracking(Set.of("1", "2"));

        StepVerifier.create(result)
                .expectTimeout(Duration.ofSeconds(1));

        verifyNoInteractions(backendServicesClient);

    }

    @Test
    void forwardRequestWhenCapIsReachedForTracking() {
        doReturn(Mono.just(Map.of("1", "track1", "2", "track2", "3", "track3")))
                .when(backendServicesClient).getTracking(Set.of("1", "2", "3"));

        Mono<Map<String, String>> result1 = bulkedServiceClient.getTracking(Set.of("1", "2"));
        Mono<Map<String, String>> result2 = bulkedServiceClient.getTracking(Set.of("3"));

        StepVerifier.create(Flux.merge(result1, result2))
                .expectNext(Map.of("1", "track1", "2", "track2"))
                .expectNext(Map.of("3", "track3"))
                .verifyComplete();

    }

    @Test
    void doNotPublishEventIfCapIsNotReachedForShipments() {
        Mono<Map<String, List<String>>> result = bulkedServiceClient.getShipments(Set.of("1", "2"));

        StepVerifier.create(result)
                .expectTimeout(Duration.ofSeconds(1));

        verifyNoInteractions(backendServicesClient);

    }

    @Test
    void forwardRequestWhenCapIsReachedForShipments() {
        doReturn(Mono.just(Map.of("1", List.of("shipment1"), "2", List.of("shipment2"), "3", List.of("shipment3"))))
                .when(backendServicesClient).getShipments(Set.of("1", "2", "3"));

        Mono<Map<String, List<String>>> result1 = bulkedServiceClient.getShipments(Set.of("1", "2"));
        Mono<Map<String, List<String>>> result2 = bulkedServiceClient.getShipments(Set.of("3"));

        StepVerifier.create(Flux.merge(result1, result2))
                .expectNext(Map.of("1", List.of("shipment1"), "2", List.of("shipment2")))
                .expectNext(Map.of("3", List.of("shipment3")))
                .verifyComplete();

    }

}