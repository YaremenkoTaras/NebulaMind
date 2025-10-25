package com.nebulamind.tradingcore.service;

import com.nebulamind.tradingcore.api.dto.PlaceOrderRequest;
import com.nebulamind.tradingcore.api.dto.RiskPolicyDto;
import com.nebulamind.tradingcore.config.NebulaMindProperties;
import com.nebulamind.tradingcore.exception.RiskLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for risk management and validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final NebulaMindProperties properties;

    /**
     * Validate risk policy for an order
     * 
     * @param request Order request with risk policy
     * @throws RiskLimitExceededException if risk limits are exceeded
     */
    public void validateRiskPolicy(PlaceOrderRequest request) {
        RiskPolicyDto policy = request.getRiskPolicy();
        
        // Check if stop loss is provided
        if (policy.getStopLossPct() == null || policy.getStopLossPct() <= 0) {
            throw new RiskLimitExceededException("Stop loss is mandatory and must be > 0");
        }
        
        // Check max % equity
        double maxPctEquity = properties.getRisk().getMaxPctEquity();
        if (policy.getMaxPctEquity() > maxPctEquity) {
            throw new RiskLimitExceededException(
                    String.format("Max %% equity %.2f exceeds limit %.2f", 
                            policy.getMaxPctEquity(), maxPctEquity));
        }
        
        // Check stop loss %
        double maxStopLossPct = properties.getRisk().getStopLossPct();
        if (policy.getStopLossPct() > maxStopLossPct) {
            log.warn("Stop loss {} exceeds recommended maximum {}", 
                    policy.getStopLossPct(), maxStopLossPct);
        }
        
        log.debug("Risk policy validated successfully for symbol={}", request.getSymbol());
    }

    /**
     * Check if daily loss limit has been exceeded
     * 
     * @return true if daily loss limit exceeded
     */
    public boolean isDailyLossLimitExceeded() {
        // TODO: Implement actual daily P&L tracking
        return false;
    }
}

