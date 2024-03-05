package com.test.aggregationservice.application.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AggregatedData {
    private Map<String, Double> pricing;
    private Map<String, String> track;
    private Map<String, List<String>> shipments;
}
