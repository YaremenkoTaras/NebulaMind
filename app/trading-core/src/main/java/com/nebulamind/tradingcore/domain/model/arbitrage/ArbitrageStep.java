package com.nebulamind.tradingcore.domain.model.arbitrage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One step in arbitrage chain (one trade)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrageStep {
    private String fromAsset;  // Валюта, яку продаємо
    private String toAsset;    // Валюта, яку купуємо
    private String symbol;     // Trading pair (e.g. BTCUSDT)
    private double rate;       // Current exchange rate
    private double minQty;     // Minimum order quantity
    private double maxQty;     // Maximum order quantity
    private int priceDecimals; // Price precision
    private int qtyDecimals;   // Quantity precision
    
    /**
     * Calculate output amount for input
     */
    public double calculateOutput(double inputAmount) {
        return inputAmount * rate;
    }
    
    /**
     * Format quantity according to decimals
     */
    public double formatQuantity(double qty) {
        double multiplier = Math.pow(10, qtyDecimals);
        return Math.floor(qty * multiplier) / multiplier;
    }
    
    /**
     * Check if quantity is within limits
     */
    public boolean isQuantityValid(double qty) {
        return qty >= minQty && qty <= maxQty;
    }
}
