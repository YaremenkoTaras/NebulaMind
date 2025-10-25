package com.nebulamind.tradingcore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Order result DTO for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResultDto {
    private String clientOrderId;
    private String orderId;
    private String symbol;
    private String side;
    private String status; // NEW | FILLED | CANCELED | REJECTED
    private double origQty;
    private double executedQty;
    private Double avgPrice;
    private Instant timestamp;
    private String message;
}

