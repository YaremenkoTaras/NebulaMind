package com.nebulamind.tradingcore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model for a trading position
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private String symbol;
    private PositionSide side;
    private double quantity;
    private double entryPrice;
    private double currentPrice;
    private double unrealizedPnL;
    private double realizedPnL;
    private Instant openedAt;
    private Instant updatedAt;
    
    // Stop loss and take profit levels
    private Double stopLoss;
    private Double takeProfit;

    public enum PositionSide {
        LONG, SHORT
    }
    
    /**
     * Calculate unrealized P&L based on current price
     */
    public void updateUnrealizedPnL(double currentPrice) {
        this.currentPrice = currentPrice;
        if (side == PositionSide.LONG) {
            this.unrealizedPnL = (currentPrice - entryPrice) * quantity;
        } else {
            this.unrealizedPnL = (entryPrice - currentPrice) * quantity;
        }
        this.updatedAt = Instant.now();
    }
}

