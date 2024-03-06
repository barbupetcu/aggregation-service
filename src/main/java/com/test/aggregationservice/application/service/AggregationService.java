package com.test.aggregationservice.application.service;

import com.test.aggregationservice.application.model.AggregatedData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final BulkedServiceClient bulkedServiceClient;

    public Mono<AggregatedData> getAggregatedData(
            Set<String> pricing,
            Set<String> track,
            Set<String> shipments
    ) {
        return Mono.zip(
                        getApiResponse(pricing, bulkedServiceClient::getPricing),
                        getApiResponse(track, bulkedServiceClient::getTracking),
                        getApiResponse(shipments, bulkedServiceClient::getShipments)
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
        return clientFunction.apply(ids);
    }
}