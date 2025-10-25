package com.nebulamind.agentbuilder.tool;

import com.nebulamind.agentbuilder.client.TradingCoreClient;
import com.nebulamind.agentbuilder.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Trading tools that LLM can call
 * 
 * These tools provide a safe abstraction over Trading Core API
 * with additional guardrails and validation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradingTools {

    private final TradingCoreClient tradingCoreClient;

    /**
     * Tool: getPortfolio
     * 
     * Returns current portfolio snapshot including equity, positions, and balance
     */
    public PortfolioDto getPortfolio() {
        log.info("[TOOL] getPortfolio() called");
        return tradingCoreClient.getPortfolio();
    }

    /**
     * Tool: placeOrderSafe
     * 
     * Places an order with mandatory risk policy
     * 
     * Guardrails:
     * - Risk policy must be provided
     * - Stop loss is mandatory
     * - Reason must be provided
     */
    public OrderResultDto placeOrderSafe(PlaceOrderRequest request) {
        log.info("[TOOL] placeOrderSafe() called: symbol={}, side={}, qty={}", 
                request.getSymbol(), request.getSide(), request.getQty());
        
        // Guardrails: validate risk policy presence
        if (request.getRiskPolicy() == null) {
            throw new IllegalArgumentException("Risk policy is mandatory for placing orders");
        }
        
        if (request.getRiskPolicy().getStopLossPct() == null || request.getRiskPolicy().getStopLossPct() <= 0) {
            throw new IllegalArgumentException("Stop loss is mandatory and must be > 0");
        }
        
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("Reason for placing order is mandatory");
        }
        
        return tradingCoreClient.placeOrderSafe(request);
    }

    /**
     * Tool: cancelOrder
     * 
     * Cancels an active order by clientOrderId
     */
    public OrderResultDto cancelOrder(String clientOrderId) {
        log.info("[TOOL] cancelOrder() called: clientOrderId={}", clientOrderId);
        
        if (clientOrderId == null || clientOrderId.isBlank()) {
            throw new IllegalArgumentException("Client order ID is required");
        }
        
        return tradingCoreClient.cancelOrder(clientOrderId);
    }

    /**
     * Tool: evaluateSignals
     * 
     * Gets trading signal for a symbol based on technical analysis
     */
    public SignalDto evaluateSignals(String symbol) {
        log.info("[TOOL] evaluateSignals() called: symbol={}", symbol);
        
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol is required");
        }
        
        return tradingCoreClient.getSignal(symbol, null);
    }
}

