package com.nebulamind.agentbuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for arbitrage scan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrageScanResponse {
    
    /**
     * List of profitable arbitrage chains found
     */
    private List<ArbitrageChainDto> chains;
    
    /**
     * Summary information
     */
    private ScanSummary summary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArbitrageChainDto {
        private String id;
        private String baseAsset;
        private List<String> steps;  // List of symbols
        private double profitPercent;
        private double minRequiredAmount;
        private String timestamp;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanSummary {
        private int totalChainsFound;
        private int profitableChainsFound;
        private double bestProfitPercent;
        private List<String> analyzedAssets;
    }
}

