package com.test.aggregationservice.api;

import com.test.aggregationservice.api.exception.ParametersNotValidException;
import com.test.aggregationservice.api.exception.ResourceNotFoundException;
import com.test.aggregationservice.application.service.AggregationService;
import io.github.adr.embedded.MADR;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Set;

@RestController
@RequestMapping("aggregation")
@RequiredArgsConstructor
@MADR(
        value = 1,
        title = "Aggregate all services responses",
        contextAndProblem = "Query all services in a single network call",
        chosenAlternative = "Used spring web flux server",
        justification = """
                Take advantage of non-blocking nature of reactive library which\s
                allow us to trigger the request to each service in parallel
                """
)
public class AggregationController {

    private final AggregationService aggregationService;

    @GetMapping
    public Mono<AggregationResponse> getAggregation(
            @RequestParam(required = false) Set<String> pricing,
            @RequestParam(required = false) Set<String> track,
            @RequestParam(required = false) Set<String> shipments
    ) {
        if (isPricingParamInvalid(pricing) && isDigitParamInvalid(track) && isDigitParamInvalid(shipments)) {
            throw  new ParametersNotValidException("provided parameters are not valid");
        }
        return aggregationService.getAggregatedData(pricing, track, shipments)
                .map(data -> new AggregationResponse(data.getPricing(), data.getTrack(), data.getShipments()))
                .filter(
                        aggregationResponse ->
                                !aggregationResponse.pricing().isEmpty() ||
                                        !aggregationResponse.shipments().isEmpty() ||
                                        !aggregationResponse.track().isEmpty()
                )
                .switchIfEmpty(
                        Mono.defer(() ->  Mono.error(new ResourceNotFoundException("Unable to find any Resource")))
                );
    }

    private boolean isPricingParamInvalid(Set<String> params) {
        return CollectionUtils.isEmpty(params) ||
                params.stream()
                        .noneMatch(param -> param.matches("[a-zA-Z]{2}"));
    }

    private boolean isDigitParamInvalid(Set<String> params) {
        return CollectionUtils.isEmpty(params) ||
                params.stream()
                        .noneMatch(param -> param.matches("[0-9]{9}"));
    }
}