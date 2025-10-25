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
public class ArbitrageExecution {
    
    private String id;
    private String chainId;
    private ArbitrageChain chain;
    private Instant timestamp;
    private Double initialAmount;
    private Double finalAmount;
    private Double profitAmount;
    private Double profitPercent;
    private String status; // COMPLETED or FAILED
    private String errorMessage;
}

