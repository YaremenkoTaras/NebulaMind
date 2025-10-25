package com.nebulamind.tradingcore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model for an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String clientOrderId;
    private String orderId;
    private String symbol;
    private OrderSide side;
    private OrderType type;
    private OrderStatus status;
    private double quantity;
    private Double price;
    private double executedQty;
    private Double avgPrice;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Risk parameters
    private Double stopLossPrice;
    private Double takeProfitPrice;
    private String reason;

    public enum OrderSide {
        BUY, SELL
    }

    public enum OrderType {
        MARKET, LIMIT
    }

    public enum OrderStatus {
        NEW, PARTIALLY_FILLED, FILLED, CANCELED, REJECTED, EXPIRED
    }
}

