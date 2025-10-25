package com.nebulamind.agentbuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionDto {
    private String symbol;
    private double quantity;
    private double entryPrice;
    private double currentPrice;
    private double unrealizedPnL;
    private String side;
}

