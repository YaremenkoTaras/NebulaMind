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
    @Builder.Default
    private double minProfitPercent = 1.0;  // Default 1%
    
    /**
     * Budget for trading (in base asset)
     */
    @Min(value = 1, message = "Budget must be positive")
    @Builder.Default
    private double budget = 100.0;  // Default 100 USDT
    
    /**
     * Task duration in minutes
     */
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Builder.Default
    private int durationMinutes = 5;  // Default 5 minutes
    
    /**
     * Delay between scans in seconds (when no profitable chains found)
     */
    @Min(value = 1, message = "Delay must be at least 1 second")
    @Builder.Default
    private int delaySeconds = 10;  // Default 10 seconds
    
    /**
     * Optional reasoning for the scan
     */
    private String reasoning;
}

