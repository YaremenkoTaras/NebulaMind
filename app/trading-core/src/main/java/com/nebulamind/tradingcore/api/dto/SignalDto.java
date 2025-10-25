package com.nebulamind.tradingcore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Trading signal DTO for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalDto {
    private String symbol;
    private String action; // BUY | SELL | HOLD
    private double confidence; // 0.0 - 1.0
    private Double suggestedQty;
    private Double suggestedPrice;
    private String reason;
    private Instant timestamp;
}

