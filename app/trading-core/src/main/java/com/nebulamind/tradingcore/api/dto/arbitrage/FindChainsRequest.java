package com.nebulamind.tradingcore.api.dto.arbitrage;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for finding arbitrage chains
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindChainsRequest {
    
    @NotBlank(message = "Base asset is required")
    private String baseAsset;
    
    @Min(value = 2, message = "Max assets must be at least 2")
    private int maxAssets;
    
    @Min(value = 2, message = "Chain length must be at least 2")
    private int chainLength;
    
    @Min(value = 0, message = "Min profit must be non-negative")
    private double minProfitPercent;
}
