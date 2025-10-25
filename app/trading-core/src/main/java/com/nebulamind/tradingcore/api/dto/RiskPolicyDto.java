package com.nebulamind.tradingcore.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Risk policy DTO for order placement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskPolicyDto {
    
    @NotNull(message = "Max % equity is required")
    @Min(value = 0, message = "Max % equity must be >= 0")
    @Max(value = 100, message = "Max % equity must be <= 100")
    private Double maxPctEquity;
    
    @NotNull(message = "Stop loss % is required")
    @Min(value = 0, message = "Stop loss % must be >= 0")
    @Max(value = 100, message = "Stop loss % must be <= 100")
    private Double stopLossPct;
    
    @Min(value = 0, message = "Take profit % must be >= 0")
    @Max(value = 100, message = "Take profit % must be <= 100")
    private Double takeProfitPct; // optional
}

