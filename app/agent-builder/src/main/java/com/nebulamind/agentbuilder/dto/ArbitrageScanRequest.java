package com.nebulamind.agentbuilder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for arbitrage scan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrageScanRequest {
    
    /**
     * Base asset to start and end with (e.g., USDT)
     */
    @NotBlank(message = "Base asset is required")
    private String baseAsset;
    
    /**
     * Maximum number of assets to analyze
     */
    @Min(value = 3, message = "Max assets must be at least 3 for triangular arbitrage")
    private int maxAssets;
    
    /**
     * Length of arbitrage chain (number of trades)
     */
    @Min(value = 3, message = "Chain length must be at least 3 for triangular arbitrage")
    private int chainLength;
    
    /**
     * Minimum profit percentage to consider
     */
    @Min(value = 0, message = "Min profit percent cannot be negative")
    private double minProfitPercent;
    
    /**
     * Optional reasoning for the scan
     */
    private String reasoning;
}

