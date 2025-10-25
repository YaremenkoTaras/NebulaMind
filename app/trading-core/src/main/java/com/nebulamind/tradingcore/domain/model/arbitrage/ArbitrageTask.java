package com.nebulamind.tradingcore.domain.model.arbitrage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Arbitrage Task - automated trading task that runs for a specified duration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrageTask {
    private String id;
    private TaskStatus status;
    
    // Configuration
    private double budget;              // Starting budget in base asset
    private int durationMinutes;        // How long task should run
    private int delaySeconds;           // Delay between scans if no profitable chains
    private String baseAsset;           // Base asset (e.g., USDT)
    private int maxAssets;              // Max assets to analyze
    private int chainLength;            // Chain length (3-5)
    private double minProfitPercent;    // Minimum profit threshold (default 1%)
    
    // Runtime state
    private Instant startTime;
    private Instant endTime;
    private double currentBalance;      // Current balance (budget + profit/loss)
    private double totalProfit;         // Total profit/loss
    private int successfulTrades;       // Count of successful trades
    private int failedTrades;           // Count of failed trades
    
    // Execution history
    @Builder.Default
    private List<ExecutedChainRecord> executedChains = new ArrayList<>();
    
    // Metadata
    private Instant createdAt;
    private Instant updatedAt;
    
    public enum TaskStatus {
        CREATED,    // Task created but not started
        RUNNING,    // Task is actively running
        STOPPED,    // Task was manually stopped
        COMPLETED,  // Task completed successfully (duration expired)
        FAILED      // Task failed with error
    }
    
    /**
     * Record of executed chain within this task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutedChainRecord {
        private String chainId;
        private double initialAmount;
        private double finalAmount;
        private double profit;
        private double profitPercent;
        private Instant timestamp;
        private String status;  // COMPLETED, FAILED
        private List<String> steps;  // Chain steps for display
    }
    
    /**
     * Check if task should still be running
     */
    public boolean shouldContinue() {
        if (status != TaskStatus.RUNNING) {
            return false;
        }
        
        if (endTime != null && Instant.now().isAfter(endTime)) {
            return false;
        }
        
        if (currentBalance <= 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate profit percentage
     */
    public double getProfitPercent() {
        if (budget == 0) return 0;
        return (totalProfit / budget) * 100;
    }
    
    /**
     * Add executed chain to history
     */
    public void addExecutedChain(ExecutedChainRecord record) {
        executedChains.add(record);
        
        // Update statistics
        currentBalance = record.getFinalAmount();
        totalProfit = currentBalance - budget;
        
        if ("COMPLETED".equals(record.getStatus())) {
            successfulTrades++;
        } else {
            failedTrades++;
        }
        
        updatedAt = Instant.now();
    }
}

