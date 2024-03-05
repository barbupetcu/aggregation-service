package com.test.aggregationservice.application.service;

import com.test.aggregationservice.application.model.AggregatedData;
import com.test.aggregationservice.infrastructure.client.BackendServicesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final BackendServicesClient client;

    public Mono<AggregatedData> getAggregatedData(
            Set<String> pricing,
            Set<String> track,
            Set<String> shipments
    ) {
        return Mono.zip(
                        getApiResponse(pricing, client::getPricing),
                        getApiResponse(track, client::getTracking),
                        getApiResponse(shipments, client::getShipments)
                )
                .map(
                        tuple ->
                                AggregatedData.builder()
                                        .pricing(tuple.getT1())
                                        .track(tuple.getT2())
                                        .shipments(tuple.getT3())
                                        .build()
                );
    }

    private <E> Mono<Map<String, E>> getApiResponse(
            Set<String> ids, Function<Set<String>, Mono<Map<String, E>>> clientFunction
    ) {
        if (CollectionUtils.isEmpty(ids)) {
            return Mono.just(Collections.emptyMap());
        }
        return Flux.fromIterable(ids)
                .map(Set::of)
                .flatMap(clientFunction)
                .collect(HashMap::new, Map::putAll);
    }
}