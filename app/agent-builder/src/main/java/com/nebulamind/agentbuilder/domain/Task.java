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
    
    @Builder.Default
    private List<ArbitrageExecution> executions = new ArrayList<>();
    
    public void addExecution(ArbitrageExecution execution) {
        this.executions.add(execution);
        this.executionsCount = this.executions.size();
        
        if (execution.getProfitAmount() > 0) {
            this.totalProfit += execution.getProfitAmount();
        } else {
            this.totalLoss += Math.abs(execution.getProfitAmount());
        }
        
        this.currentBudget += execution.getProfitAmount();
    }
}

