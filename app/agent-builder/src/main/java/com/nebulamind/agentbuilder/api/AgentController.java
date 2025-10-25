package com.nebulamind.agentbuilder.api;

import com.nebulamind.agentbuilder.dto.*;
import com.nebulamind.agentbuilder.tool.TradingTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Agent Builder
 * 
 * Provides endpoints for invoking LLM tools
 */
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final TradingTools tradingTools;

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Agent Builder is running");
    }

    /**
     * Get portfolio (tool: getPortfolio)
     */
    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioDto> getPortfolio() {
        log.info("GET /api/agent/portfolio");
        PortfolioDto portfolio = tradingTools.getPortfolio();
        return ResponseEntity.ok(portfolio);
    }

    /**
     * Place order safely (tool: placeOrderSafe)
     */
    @PostMapping("/orders/place")
    public ResponseEntity<OrderResultDto> placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("POST /api/agent/orders/place: symbol={}, side={}", 
                request.getSymbol(), request.getSide());
        OrderResultDto result = tradingTools.placeOrderSafe(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Cancel order (tool: cancelOrder)
     */
    @PostMapping("/orders/cancel/{clientOrderId}")
    public ResponseEntity<OrderResultDto> cancelOrder(@PathVariable String clientOrderId) {
        log.info("POST /api/agent/orders/cancel/{}", clientOrderId);
        OrderResultDto result = tradingTools.cancelOrder(clientOrderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Evaluate signals (tool: evaluateSignals)
     */
    @GetMapping("/signals/{symbol}")
    public ResponseEntity<SignalDto> evaluateSignals(@PathVariable String symbol) {
        log.info("GET /api/agent/signals/{}", symbol);
        SignalDto signal = tradingTools.evaluateSignals(symbol);
        return ResponseEntity.ok(signal);
    }
}

