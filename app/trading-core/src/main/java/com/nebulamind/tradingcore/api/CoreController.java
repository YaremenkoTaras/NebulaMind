package com.nebulamind.tradingcore.api;

import com.nebulamind.tradingcore.api.dto.*;
import com.nebulamind.tradingcore.service.OrderService;
import com.nebulamind.tradingcore.service.PortfolioService;
import com.nebulamind.tradingcore.service.SignalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Main REST controller for Trading Core API
 * 
 * Provides endpoints for:
 * - Portfolio queries
 * - Order placement and cancellation
 * - Trading signals
 */
@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
@Slf4j
public class CoreController {

    private final PortfolioService portfolioService;
    private final OrderService orderService;
    private final SignalService signalService;

    /**
     * Get current portfolio snapshot
     * 
     * @return Portfolio with equity, positions, and balance
     */
    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioDto> getPortfolio() {
        log.info("GET /api/core/portfolio");
        PortfolioDto portfolio = portfolioService.getPortfolio();
        return ResponseEntity.ok(portfolio);
    }

    /**
     * Place order with risk policy validation
     * 
     * @param request Order details with risk policy
     * @return Order result with ID and status
     */
    @PostMapping("/orders/placeSafe")
    public ResponseEntity<OrderResultDto> placeOrderSafe(@Valid @RequestBody PlaceOrderRequest request) {
        log.info("POST /api/core/orders/placeSafe: symbol={}, side={}, qty={}, reason={}",
                request.getSymbol(), request.getSide(), request.getQty(), request.getReason());
        
        OrderResultDto result = orderService.placeOrderSafe(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Cancel an active order
     * 
     * @param request Cancel request with client order ID
     * @return Order result with cancellation status
     */
    @PostMapping("/orders/cancel")
    public ResponseEntity<OrderResultDto> cancelOrder(@Valid @RequestBody CancelOrderRequest request) {
        log.info("POST /api/core/orders/cancel: clientOrderId={}", request.getClientOrderId());
        
        OrderResultDto result = orderService.cancelOrder(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Get trading signal for a symbol
     * 
     * @param symbol Trading pair symbol (e.g., BTCUSDT)
     * @param asOf Optional timestamp for historical signal (ISO-8601)
     * @return Trading signal with action and reasoning
     */
    @GetMapping("/signal")
    public ResponseEntity<SignalDto> getSignal(
            @RequestParam String symbol,
            @RequestParam(required = false) Instant asOf) {
        log.info("GET /api/core/signal: symbol={}, asOf={}", symbol, asOf);
        
        SignalDto signal = signalService.evaluateSignal(symbol, asOf);
        return ResponseEntity.ok(signal);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Trading Core is running");
    }
}

