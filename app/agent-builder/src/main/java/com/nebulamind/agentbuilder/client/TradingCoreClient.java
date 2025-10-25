package com.nebulamind.agentbuilder.client;

import com.nebulamind.agentbuilder.config.AgentProperties;
import com.nebulamind.agentbuilder.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Client for calling Trading Core API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradingCoreClient {

    private final WebClient coreApiClient;
    private final AgentProperties properties;

    /**
     * Get current portfolio snapshot
     */
    public PortfolioDto getPortfolio() {
        log.debug("Calling Trading Core: GET /api/core/portfolio");
        
        return coreApiClient.get()
                .uri("/api/core/portfolio")
                .retrieve()
                .bodyToMono(PortfolioDto.class)
                .timeout(Duration.ofSeconds(properties.getCoreApi().getTimeoutSeconds()))
                .doOnError(e -> log.error("Failed to get portfolio", e))
                .block();
    }

    /**
     * Place order with risk policy
     */
    public OrderResultDto placeOrderSafe(PlaceOrderRequest request) {
        log.debug("Calling Trading Core: POST /api/core/orders/placeSafe");
        
        return coreApiClient.post()
                .uri("/api/core/orders/placeSafe")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderResultDto.class)
                .timeout(Duration.ofSeconds(properties.getCoreApi().getTimeoutSeconds()))
                .doOnError(e -> log.error("Failed to place order", e))
                .block();
    }

    /**
     * Cancel an order
     */
    public OrderResultDto cancelOrder(String clientOrderId) {
        log.debug("Calling Trading Core: POST /api/core/orders/cancel");
        
        CancelOrderRequest request = CancelOrderRequest.builder()
                .clientOrderId(clientOrderId)
                .build();
        
        return coreApiClient.post()
                .uri("/api/core/orders/cancel")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderResultDto.class)
                .timeout(Duration.ofSeconds(properties.getCoreApi().getTimeoutSeconds()))
                .doOnError(e -> log.error("Failed to cancel order", e))
                .block();
    }

    /**
     * Get trading signal for a symbol
     */
    public SignalDto getSignal(String symbol, Instant asOf) {
        log.debug("Calling Trading Core: GET /api/core/signal?symbol={}", symbol);
        
        return coreApiClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/core/signal")
                            .queryParam("symbol", symbol);
                    if (asOf != null) {
                        builder.queryParam("asOf", asOf.toString());
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(SignalDto.class)
                .timeout(Duration.ofSeconds(properties.getCoreApi().getTimeoutSeconds()))
                .doOnError(e -> log.error("Failed to get signal", e))
                .block();
    }
}

