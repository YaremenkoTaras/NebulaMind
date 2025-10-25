package com.nebulamind.agentbuilder.tool.arbitrage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Market information for a trading pair
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketInfo {
    private String symbol;
    private String baseAsset;
    private String quoteAsset;
    private double lastPrice;
    private double volume24h;
    private double minQty;
    private double maxQty;
    private int priceDecimals;
    private int qtyDecimals;
    private boolean isActive;
}
