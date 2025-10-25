package com.nebulamind.agentbuilder.tool.arbitrage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO with market analysis results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeMarketsResponse {
    private List<ArbitrageSuggestion> suggestions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArbitrageSuggestion {
        private List<String> steps;
        private String reasoning;
        private double estimatedProfitPercent;
    }
}
