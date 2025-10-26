package com.nebulamind.tradingcore.api.dto.arbitrage;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for executing arbitrage chain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteChainRequest {
    
    @Positive(message = "Base amount must be positive")
    private double baseAmount;
}
