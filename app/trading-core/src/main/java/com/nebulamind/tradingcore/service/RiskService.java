package com.nebulamind.tradingcore.service;

import com.nebulamind.tradingcore.api.dto.PlaceOrderRequest;
import com.nebulamind.tradingcore.api.dto.RiskPolicyDto;
import com.nebulamind.tradingcore.config.NebulaMindProperties;
import com.nebulamind.tradingcore.exception.RiskLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for API-level risk validation
 * 
 * Note: Domain-level risk management is handled by DefaultRiskManager
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final NebulaMindProperties properties;

    /**
     * Validate risk policy from API request
     * 
     * @param request Order request with risk policy
     * @throws RiskLimitExceededException if risk policy is invalid
     */
    public void validateRiskPolicy(PlaceOrderRequest request) {
        RiskPolicyDto policy = request.getRiskPolicy();
        
        if (policy == null) {
            throw new RiskLimitExceededException("Risk policy is mandatory");
        }
        
        // Check if stop loss is provided
        if (policy.getStopLossPct() == null || policy.getStopLossPct() <= 0) {
            throw new RiskLimitExceededException("Stop loss is mandatory and must be > 0");
        }
        
        // Check max % equity
        if (policy.getMaxPctEquity() == null) {
            throw new RiskLimitExceededException("Max % equity is mandatory");
        }
        
        double maxPctEquity = properties.getRisk().getMaxPctEquity();
        if (policy.getMaxPctEquity() > maxPctEquity) {
            throw new RiskLimitExceededException(
                    String.format("Max %% equity %.2f exceeds limit %.2f", 
                            policy.getMaxPctEquity(), maxPctEquity));
        }
        
        log.debug("Risk policy validated successfully for symbol={}", request.getSymbol());
    }
}

