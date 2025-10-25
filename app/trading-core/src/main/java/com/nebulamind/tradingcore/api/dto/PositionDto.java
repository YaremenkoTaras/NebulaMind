package com.nebulamind.tradingcore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Position DTO for portfolio positions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionDto {
    private String symbol;
    private double quantity;
    private double entryPrice;
    private double currentPrice;
    private double unrealizedPnL;
    private String side; // LONG | SHORT
}

