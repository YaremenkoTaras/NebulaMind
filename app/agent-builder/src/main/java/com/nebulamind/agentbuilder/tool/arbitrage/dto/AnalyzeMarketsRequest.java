package com.nebulamind.agentbuilder.tool.arbitrage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for market analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeMarketsRequest {
    private String baseAsset;
    private int maxAssets;
    private int chainLength;
    private double minVolumeUsd;
}
