package com.nebulamind.agentbuilder.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArbitrageChain {
    
    private String id;
    private String baseAsset;
    private List<String> steps;
    private Double profitPercent;
    private Double minRequiredAmount;
    private Instant timestamp;
}

