package com.test.aggregationservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(BackendServicesProperties.class)
public class BackendServicesRestConfig {

    public static final String BACKEND_SERVICE_CLIENT = "BACKEND_SERVICE_CLIENT";

    @Bean(BACKEND_SERVICE_CLIENT)
    public WebClient backendServicesClient(@Value("${endpoints.backend-services}") String backendServicesUrl) {
        return WebClient.builder()
                .baseUrl(backendServicesUrl)
                .build();
    }
}
