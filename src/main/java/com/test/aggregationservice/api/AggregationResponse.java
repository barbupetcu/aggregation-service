package com.test.aggregationservice.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AggregationResponse(
        Map<String, Double> pricing, Map<String, String> track, Map<String, List<String>> shipments
) {
}