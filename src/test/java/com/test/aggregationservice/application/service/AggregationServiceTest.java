package com.test.aggregationservice.application.service;

import com.test.aggregationservice.application.model.AggregatedData;
import com.test.aggregationservice.infrastructure.client.BackendServicesClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class AggregationServiceTest {

    private static final Set<String> TRACK_PARAMS = Set.of("1", "2");
    private static final Set<String> PRICING_PARAMS = Set.of("A", "B");
    private static final Set<String> SHIPMENTS_PARAMS = Set.of("3", "4");

    @InjectMocks
    private AggregationService aggregationService;
    @Mock
    private BackendServicesClient backendServicesClient;

    @Test
    void aggregateResponseFromAllApisSuccessfully() {
        doReturn(Mono.just(Map.of("1", "COLLECTING"))).when(backendServicesClient).getTracking(Set.of("1"));
        doReturn(Mono.just(Map.of("2", "DELIVERING"))).when(backendServicesClient).getTracking(Set.of("2"));

        doReturn(Mono.just(Map.of("3", List.of("pallet", "envelope"))))
                .when(backendServicesClient).getShipments(Set.of("3"));
        doReturn(Mono.just(Map.of("4", List.of("box")))).when(backendServicesClient).getShipments(Set.of("4"));

        doReturn(Mono.just(Map.of("A", 1.0D))).when(backendServicesClient).getPricing(Set.of("A"));
        doReturn(Mono.just(Map.of("B", 2.0D))).when(backendServicesClient).getPricing(Set.of("B"));

        Mono<AggregatedData> result = aggregationService.getAggregatedData(PRICING_PARAMS, TRACK_PARAMS, SHIPMENTS_PARAMS);

        StepVerifier.create(result)
                .expectNextMatches(
                        data -> data.getPricing().equals(Map.of("A", 1.0D, "B", 2.0D)) &&
                                data.getTrack().equals(Map.of("1", "COLLECTING", "2", "DELIVERING")) &&
                                data.getShipments().equals(Map.of("3", List.of("pallet", "envelope"), "4", List.of("box")))

                )
                .verifyComplete();
    }

    @Test
    void filterOutUnWantedRequests() {
        doReturn(Mono.just(Map.of("1", "COLLECTING"))).when(backendServicesClient).getTracking(Set.of("1"));
        doReturn(Mono.just(Map.of("2", "DELIVERING"))).when(backendServicesClient).getTracking(Set.of("2"));

        Mono<AggregatedData> result = aggregationService.getAggregatedData(null, TRACK_PARAMS, Collections.emptySet());

        StepVerifier.create(result)
                .expectNextMatches(
                        data -> data.getPricing().isEmpty() &&
                                data.getTrack().equals(Map.of("1", "COLLECTING", "2", "DELIVERING")) &&
                                data.getShipments().isEmpty()

                )
                .verifyComplete();

        verifyNoMoreInteractions(backendServicesClient);
    }

}