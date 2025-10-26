package com.nebulamind.agentbuilder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {
    
    @NotBlank(message = "Base asset is required")
    private String baseAsset;
    
    @NotNull(message = "Budget is required")
    @Min(value = 1, message = "Budget must be at least 1")
    private Double budget;
    
    @NotNull(message = "Execution time is required")
    @Min(value = 1, message = "Execution time must be at least 1 minute")
    private Integer executionTimeMinutes;
    
    @NotNull(message = "Delay is required")
    @Min(value = 1, message = "Delay must be at least 1 second")
    private Integer delaySeconds;
    
    @NotNull(message = "Min profit percent is required")
    @Min(value = 0, message = "Min profit percent must be non-negative")
    private Double minProfitPercent;
    
    @NotNull(message = "Max assets is required")
    @Min(value = 3, message = "Max assets must be at least 3")
    private Integer maxAssets;
    
    @NotNull(message = "Chain length is required")
    @Min(value = 3, message = "Chain length must be at least 3")
    private Integer chainLength;
    
    // Advanced settings with defaults
    private Double slippageTolerance = 0.5; // Default 0.5% (changed from 1.0%)
    private Double maxLossPerTrade = 1.0;   // Max 1% loss per trade
    private Boolean enableCircuitBreaker = true;
    private Boolean enableSmartSizing = true;
}

