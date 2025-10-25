package com.nebulamind.tradingcore.service;

import com.nebulamind.tradingcore.api.dto.CancelOrderRequest;
import com.nebulamind.tradingcore.api.dto.OrderResultDto;
import com.nebulamind.tradingcore.api.dto.PlaceOrderRequest;
import com.nebulamind.tradingcore.domain.model.Order;
import com.nebulamind.tradingcore.domain.model.Portfolio;
import com.nebulamind.tradingcore.domain.port.ExchangeGateway;
import com.nebulamind.tradingcore.domain.port.RiskManager;
import com.nebulamind.tradingcore.exception.OrderValidationException;
import com.nebulamind.tradingcore.exception.RiskLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for order operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final ExchangeGateway exchangeGateway;
    private final RiskManager riskManager;

    /**
     * Place order with risk policy validation
     * 
     * @param request Order details with risk policy
     * @return Order result
     */
    public OrderResultDto placeOrderSafe(PlaceOrderRequest request) {
        log.info("Placing order: symbol={}, side={}, qty={}", 
                request.getSymbol(), request.getSide(), request.getQty());
        
        // Get current portfolio
        Portfolio portfolio = exchangeGateway.getPortfolio();
        
        // Create order domain model
        Order order = Order.builder()
                .clientOrderId("ORDER_" + UUID.randomUUID().toString().substring(0, 8))
                .symbol(request.getSymbol())
                .side(Order.OrderSide.valueOf(request.getSide()))
                .type(request.getLimitPrice() != null ? Order.OrderType.LIMIT : Order.OrderType.MARKET)
                .quantity(request.getQty())
                .price(request.getLimitPrice())
                .reason(request.getReason())
                .build();
        
        // Calculate stop loss and take profit prices
        if (request.getRiskPolicy() != null) {
            double basePrice = request.getLimitPrice() != null ? 
                    request.getLimitPrice() : exchangeGateway.getCurrentPrice(request.getSymbol());
            
            if (request.getRiskPolicy().getStopLossPct() != null) {
                double slPct = request.getRiskPolicy().getStopLossPct() / 100.0;
                order.setStopLossPrice(request.getSide().equals("BUY") ? 
                        basePrice * (1 - slPct) : basePrice * (1 + slPct));
            }
            
            if (request.getRiskPolicy().getTakeProfitPct() != null) {
                double tpPct = request.getRiskPolicy().getTakeProfitPct() / 100.0;
                order.setTakeProfitPrice(request.getSide().equals("BUY") ? 
                        basePrice * (1 + tpPct) : basePrice * (1 - tpPct));
            }
        }
        
        // Validate with risk manager
        RiskManager.ValidationResult validation = riskManager.validateOrder(order, portfolio);
        if (!validation.valid()) {
            throw new RiskLimitExceededException(validation.message());
        }
        
        // Execute order via exchange gateway
        Order executedOrder = exchangeGateway.placeOrder(order);
        
        // Record trade
        if (executedOrder.getStatus() == Order.OrderStatus.FILLED) {
            double pnl = 0.0; // Will be calculated when position is closed
            riskManager.recordTrade(executedOrder, pnl);
        }
        
        return toDto(executedOrder);
    }

    /**
     * Cancel an active order
     * 
     * @param request Cancel request
     * @return Order result with cancellation status
     */
    public OrderResultDto cancelOrder(CancelOrderRequest request) {
        log.info("Canceling order: clientOrderId={}", request.getClientOrderId());
        
        Order canceledOrder = exchangeGateway.cancelOrder(request.getClientOrderId());
        return toDto(canceledOrder);
    }
    
    private OrderResultDto toDto(Order order) {
        return OrderResultDto.builder()
                .clientOrderId(order.getClientOrderId())
                .orderId(order.getOrderId())
                .symbol(order.getSymbol())
                .side(order.getSide().name())
                .status(order.getStatus().name())
                .origQty(order.getQuantity())
                .executedQty(order.getExecutedQty())
                .avgPrice(order.getAvgPrice())
                .timestamp(order.getUpdatedAt() != null ? order.getUpdatedAt() : Instant.now())
                .message("Order processed successfully")
                .build();
    }
}

