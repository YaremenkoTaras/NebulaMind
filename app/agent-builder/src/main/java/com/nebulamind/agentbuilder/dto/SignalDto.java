package com.nebulamind.agentbuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalDto {
    private String symbol;
    private String action; // BUY | SELL | HOLD
    private double confidence;
    private Double suggestedQty;
    private Double suggestedPrice;
    private String reason;
    private Instant timestamp;
}

