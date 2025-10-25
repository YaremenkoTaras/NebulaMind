package com.nebulamind.tradingcore.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for placing an order with risk policy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    
    @NotBlank(message = "Symbol is required")
    private String symbol;
    
    @NotNull(message = "Side is required")
    @Pattern(regexp = "BUY|SELL", message = "Side must be BUY or SELL")
    private String side;
    
    @Positive(message = "Quantity must be positive")
    private double qty;
    
    private Double limitPrice; // null = market order
    
    @NotNull(message = "Risk policy is required")
    @Valid
    private RiskPolicyDto riskPolicy;
    
    @NotBlank(message = "Reason is required")
    @Size(min = 3, message = "Reason must be at least 3 characters")
    private String reason;
}

