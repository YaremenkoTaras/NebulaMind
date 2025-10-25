package com.nebulamind.tradingcore.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for canceling an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    
    @NotBlank(message = "Client order ID is required")
    @Size(min = 8, message = "Client order ID must be at least 8 characters")
    private String clientOrderId;
}

