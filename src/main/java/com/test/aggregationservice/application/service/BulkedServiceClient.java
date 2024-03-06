package com.test.aggregationservice.application.service;

import com.test.aggregationservice.infrastructure.client.BackendServicesClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkedServiceClient {

    private final BackendServicesClient backendServicesClient;
    @Value("${request.bulk-size}")
    private final int bulkSize;

    private final Sinks.Many<Tuple2<String, Sinks.One<Tuple2<String, Double>>>> pricingQueue =
            Sinks.many().unicast().onBackpressureBuffer();
    private final Sinks.Many<Tuple2<String, Sinks.One<Tuple2<String, String>>>> trackingQueue =
            Sinks.many().unicast().onBackpressureBuffer();
    private final Sinks.Many<Tuple2<String, Sinks.One<Tuple2<String, List<String>>>>> shipmentsQueue =
            Sinks.many().unicast().onBackpressureBuffer();

    @PostConstruct
    void init() {
        pricingQueue.asFlux()
                .buffer(bulkSize)
                .flatMap(bulk -> handleBulkRequest(bulk, backendServicesClient::getPricing))
                .subscribe();

        trackingQueue.asFlux()
                .buffer(bulkSize)
                .flatMap(bulk -> handleBulkRequest(bulk, backendServicesClient::getTracking))
                .subscribe();

        shipmentsQueue.asFlux()
                .buffer(bulkSize)
                .flatMap(bulk -> handleBulkRequest(bulk, backendServicesClient::getShipments))
                .subscribe();
    }

    private <V> Mono<Void> handleBulkRequest(
            List<Tuple2<String, Sinks.One<Tuple2<String, V>>>> bulk,
            Function<Set<String>, Mono<Map<String, V>>> clientCall
    ) {
        Set<String> params = bulk.stream().map(Tuple2::t1).collect(Collectors.toSet());
        return clientCall.apply(params)
                .doOnNext(
                        responseMap -> responseMap
                                .forEach(
                                        (k, v) -> bulk.stream()
                                                .filter(tuple2 -> tuple2.t1.equals(k))
                                                .map(Tuple2::t2)
                                                .forEach(callback -> callback.tryEmitValue(new Tuple2<>(k, v)))
                                )
                )
                .then();
    }

    public Mono<Map<String, Double>> getPricing(Set<String> pricingIds) {
        return getBulkedResponse(pricingIds, pricingQueue);
    }

    public Mono<Map<String, String>> getTracking(Set<String> trackingIds) {
        return getBulkedResponse(trackingIds, trackingQueue);
    }

    public Mono<Map<String, List<String>>> getShipments(Set<String> shipmentsIds) {
        return getBulkedResponse(shipmentsIds, shipmentsQueue);
    }

    private <V> Mono<Map<String, V>> getBulkedResponse(
            Set<String> pricingIds,
            Sinks.Many<Tuple2<String, Sinks.One<Tuple2<String, V>>>> queue
    ) {
        return Flux.fromIterable(pricingIds)
                .flatMapSequential(id -> getSingleResponse(id, queue))
                .collectMap(Tuple2::t1, Tuple2::t2);
    }

    private <V> Mono<Tuple2<String, V>> getSingleResponse(
            String id,
            Sinks.Many<Tuple2<String, Sinks.One<Tuple2<String, V>>>> queue
    ) {
        Sinks.One<Tuple2<String, V>> callback = Sinks.one();
        queue.tryEmitNext(new Tuple2<>(id, callback));
        return callback.asMono();
    }

    private record Tuple2<T1, T2>(T1 t1, T2 t2) {
    }
}
