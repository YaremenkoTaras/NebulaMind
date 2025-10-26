package com.nebulamind.tradingcore.api.dto.arbitrage;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create arbitrage task
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    
    @NotBlank
    private String baseAsset;
    
    @Min(3)
    private int maxAssets;
    
    @Min(3)
    private int chainLength;
    
    @Min(0)
    @Builder.Default
    private double minProfitPercent = 1.0;
    
    @Min(1)
    @Builder.Default
    private double budget = 100.0;
    
    @Min(1)
    @Builder.Default
    private int durationMinutes = 5;
    
    @Min(1)
    @Builder.Default
    private int delaySeconds = 10;
}

