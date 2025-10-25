package com.nebulamind.agentbuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    private String symbol;
    private String side; // BUY | SELL
    private double qty;
    private Double limitPrice;
    private RiskPolicyDto riskPolicy;
    private String reason;
}

