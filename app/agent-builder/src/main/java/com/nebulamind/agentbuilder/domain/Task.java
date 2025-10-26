package com.nebulamind.agentbuilder.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    
    private String id;
    private TaskStatus status;
    private String baseAsset;
    private Double budget;
    private Double currentBudget;
    private Integer executionTimeMinutes;
    private Integer delaySeconds;
    private Double minProfitPercent;
    private Integer maxAssets;
    private Integer chainLength;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private Double totalProfit;
    private Double totalLoss;
    private Integer executionsCount;
    
    // Advanced settings
    private Double slippageTolerance;
    private Double maxLossPerTrade;
    private Boolean enableCircuitBreaker;
    private Boolean enableSmartSizing;
    
    // Runtime tracking
    @Builder.Default
    private Integer consecutiveLosses = 0;
    @Builder.Default
    private Integer consecutiveWins = 0;
    private Double maxDrawdown;
    private String stoppedReason;
    
    @Builder.Default
    private List<ArbitrageExecution> executions = new ArrayList<>();
    
    public void addExecution(ArbitrageExecution execution) {
        this.executions.add(execution);
        this.executionsCount = this.executions.size();
        
        double profitAmount = execution.getProfitAmount();
        
        if (profitAmount > 0) {
            this.totalProfit += profitAmount;
            this.consecutiveWins++;
            this.consecutiveLosses = 0;
        } else {
            this.totalLoss += Math.abs(profitAmount);
            this.consecutiveLosses++;
            this.consecutiveWins = 0;
        }
        
        this.currentBudget += profitAmount;
        
        // Track max drawdown
        double currentDrawdown = (this.totalLoss - this.totalProfit) / this.budget * 100;
        if (this.maxDrawdown == null || currentDrawdown > this.maxDrawdown) {
            this.maxDrawdown = currentDrawdown;
        }
    }
}

