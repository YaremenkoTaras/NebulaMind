package com.nebulamind.agentbuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArbitrageExecutionDto {
    
    private String id;
    private String chainId;
    private ArbitrageChainDto chain;
    private Instant timestamp;
    private Double initialAmount;
    private Double finalAmount;
    private Double profitAmount;
    private Double profitPercent;
    private Double expectedProfitPercent; // Predicted profit at scan time
    private String status; // COMPLETED or FAILED
    private String errorMessage;
}

