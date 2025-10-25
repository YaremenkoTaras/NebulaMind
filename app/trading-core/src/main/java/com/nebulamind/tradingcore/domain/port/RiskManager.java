package com.nebulamind.tradingcore.domain.port;

import com.nebulamind.tradingcore.domain.model.Order;
import com.nebulamind.tradingcore.domain.model.Portfolio;

/**
 * Port interface for risk management
 */
public interface RiskManager {
    
    /**
     * Validate order against risk policies
     * 
     * @param order Order to validate
     * @param portfolio Current portfolio
     * @return ValidationResult with outcome and message
     */
    ValidationResult validateOrder(Order order, Portfolio portfolio);
    
    /**
     * Check if daily loss limit has been exceeded
     * 
     * @return true if daily loss limit exceeded
     */
    boolean isDailyLossLimitExceeded();
    
    /**
     * Record trade result for risk tracking
     * 
     * @param order Executed order
     * @param pnl Profit/Loss from the trade
     */
    void recordTrade(Order order, double pnl);
    
    /**
     * Result of validation
     */
    record ValidationResult(boolean valid, String message) {
        public static ValidationResult ok() {
            return new ValidationResult(true, "Order passed validation");
        }
        
        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }
    }
}

