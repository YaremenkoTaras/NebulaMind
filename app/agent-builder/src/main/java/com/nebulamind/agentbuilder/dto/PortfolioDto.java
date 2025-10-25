package com.nebulamind.agentbuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDto {
    private Instant timestamp;
    private double totalEquity;
    private double freeBalance;
    private double lockedBalance;
    private List<PositionDto> positions;
    private String currency;
}

