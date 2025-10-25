package com.nebulamind.agentbuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskPolicyDto {
    private Double maxPctEquity;
    private Double stopLossPct;
    private Double takeProfitPct;
}

