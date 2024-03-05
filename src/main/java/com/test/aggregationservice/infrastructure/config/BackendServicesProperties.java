package com.test.aggregationservice.infrastructure.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "backend-services")
public class BackendServicesProperties {

    private final String shipments;
    private final String track;
    private final String pricing;
}
