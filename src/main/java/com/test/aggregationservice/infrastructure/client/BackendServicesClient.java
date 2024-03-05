package com.test.aggregationservice.infrastructure.client;

import com.test.aggregationservice.infrastructure.config.BackendServicesProperties;
import com.test.aggregationservice.infrastructure.config.BackendServicesRestConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackendServicesClient {

    private static final String QUERY_PARAM = "q";

    @Qualifier(BackendServicesRestConfig.BACKEND_SERVICE_CLIENT)
    private final WebClient webClient;
    private final BackendServicesProperties properties;

    private <E> Mono<Map<String, E>> getBackendServiceData(String path, Set<String> ids) {
        ParameterizedTypeReference<Map<String, E>> type = new ParameterizedTypeReference<>(){};
        return webClient.get()
                .uri(uri -> uri.pathSegment(path)
                        .queryParam(QUERY_PARAM, ids)
                        .build()
                )
                .retrieve()
                .bodyToMono(type)
                .doOnError(
                        error -> log.warn(
                                "Unable to get response for path [/{}] and ids {}. {}", path, ids, error.getMessage()
                        )
                )
                .onErrorResume(error -> Mono.just(getMapWithNullValues(ids)));
    }

    public Mono<Map<String, Double>> getPricing(Set<String> pricingIds) {
        return getBackendServiceData(properties.getPricing(), pricingIds);
    }

    public Mono<Map<String, String>> getTracking(Set<String> trackingIds) {
        return getBackendServiceData(properties.getTrack(), trackingIds);
    }

    public Mono<Map<String, List<String>>> getShipments(Set<String> shipmentsIds) {
        return getBackendServiceData(properties.getShipments(), shipmentsIds);
    }

    private <E> Map<String, E> getMapWithNullValues(Set<String> ids) {
        Map<String, E> result = new HashMap<>();
        ids.forEach(id -> result.put(id, null));
        return result;
    }
}
