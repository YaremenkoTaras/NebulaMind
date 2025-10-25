package com.nebulamind.agentbuilder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuration for WebClient (HTTP client for calling Trading Core)
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient coreApiClient(AgentProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getCoreApi().getBaseUrl())
                .defaultHeaders(headers -> {
                    headers.add("User-Agent", "NebulaMind-Agent/" + properties.getName());
                })
                .build();
    }
}

