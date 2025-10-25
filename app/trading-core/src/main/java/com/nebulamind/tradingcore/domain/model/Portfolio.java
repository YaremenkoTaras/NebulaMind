package com.nebulamind.tradingcore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for portfolio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    private String accountId;
    private double totalEquity;
    private double freeBalance;
    private double lockedBalance;
    private String currency;
    private Instant timestamp;
    
    @Builder.Default
    private List<Position> positions = new ArrayList<>();
    
    /**
     * Calculate total equity including positions
     */
    public void calculateEquity() {
        double positionsValue = positions.stream()
                .mapToDouble(p -> p.getQuantity() * p.getCurrentPrice())
                .sum();
        this.totalEquity = freeBalance + lockedBalance + positionsValue;
        this.timestamp = Instant.now();
    }
    
    /**
     * Get position by symbol
     */
    public Position getPosition(String symbol) {
        return positions.stream()
                .filter(p -> p.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Add or update position
     */
    public void updatePosition(Position position) {
        Position existing = getPosition(position.getSymbol());
        if (existing != null) {
            positions.remove(existing);
        }
        positions.add(position);
        calculateEquity();
    }
    
    /**
     * Remove position
     */
    public void removePosition(String symbol) {
        positions.removeIf(p -> p.getSymbol().equals(symbol));
        calculateEquity();
    }
}

