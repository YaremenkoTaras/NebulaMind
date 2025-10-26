package com.nebulamind.agentbuilder.service;

import com.nebulamind.agentbuilder.domain.*;
import com.nebulamind.agentbuilder.dto.ArbitrageScanRequest;
import com.nebulamind.agentbuilder.dto.ArbitrageScanResponse;
import com.nebulamind.agentbuilder.dto.TaskDto;
import com.nebulamind.agentbuilder.tool.ArbitrageTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for automatically executing arbitrage tasks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArbitrageTaskExecutor {
    
    private final TaskService taskService;
    private final ArbitrageTools arbitrageTools;
    private final Map<String, Instant> lastExecutionTime = new ConcurrentHashMap<>();
    
    // Circuit Breaker Pattern
    private final Map<String, Integer> chainFailureCount = new ConcurrentHashMap<>();
    private final Map<String, Instant> chainBlacklist = new ConcurrentHashMap<>();
    private static final int FAILURE_THRESHOLD = 3;
    private static final int BLACKLIST_DURATION_MINUTES = 5;
    
    // Chain success tracking
    private final Map<String, ChainStats> chainStatsMap = new ConcurrentHashMap<>();
    
    // Scan result caching
    private final Map<String, CachedScanResult> scanCache = new ConcurrentHashMap<>();
    private static final int SCAN_CACHE_VALIDITY_SECONDS = 20;
    
    @lombok.Data
    static class ChainStats {
        private int totalExecutions = 0;
        private int successfulExecutions = 0;
        private double totalSlippage = 0.0;
        
        public double getSuccessRate() {
            return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
        }
        
        public double getAverageSlippage() {
            return totalExecutions > 0 ? totalSlippage / totalExecutions : 0.0;
        }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    static class CachedScanResult {
        private ArbitrageScanResponse response;
        private Instant timestamp;
        
        public boolean isValid() {
            return Duration.between(timestamp, Instant.now()).getSeconds() < SCAN_CACHE_VALIDITY_SECONDS;
        }
    }
    
    /**
     * Process running tasks - checks every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void processRunningTasks() {
        List<TaskDto> tasks = taskService.getAllTasks();
        
        for (TaskDto taskDto : tasks) {
            if (taskDto.getStatus() == TaskStatus.RUNNING) {
                processTask(taskDto);
            }
        }
    }
    
    private void processTask(TaskDto taskDto) {
        try {
            // Check if task should be completed (time expired)
            if (shouldCompleteTask(taskDto)) {
                taskService.completeTask(taskDto.getId());
                log.info("Task {} completed: execution time expired", taskDto.getId());
                return;
            }
            
            // Emergency stop conditions
            if (shouldEmergencyStop(taskDto)) {
                String reason = getEmergencyStopReason(taskDto);
                taskService.emergencyStopTask(taskDto.getId(), reason);
                log.warn("Task {} emergency stopped: {}", taskDto.getId(), reason);
                return;
            }
            
            // Check if we should wait (delay)
            if (shouldWaitForDelay(taskDto)) {
                return;
            }
            
            // Execute arbitrage scan and trade
            executeArbitrageCycle(taskDto);
            
            // Update last execution time
            lastExecutionTime.put(taskDto.getId(), Instant.now());
            
        } catch (Exception e) {
            log.error("Error processing task {}: {}", taskDto.getId(), e.getMessage(), e);
            taskService.failTask(taskDto.getId(), e.getMessage());
        }
    }
    
    private boolean shouldCompleteTask(TaskDto task) {
        if (task.getStartedAt() == null) {
            return false;
        }
        
        Duration elapsed = Duration.between(task.getStartedAt(), Instant.now());
        Duration maxDuration = Duration.ofMinutes(task.getExecutionTimeMinutes());
        
        return elapsed.compareTo(maxDuration) >= 0;
    }
    
    private boolean shouldWaitForDelay(TaskDto task) {
        Instant lastExecution = lastExecutionTime.get(task.getId());
        
        if (lastExecution == null) {
            return false; // First execution
        }
        
        Duration timeSinceLastExecution = Duration.between(lastExecution, Instant.now());
        Duration requiredDelay = Duration.ofSeconds(task.getDelaySeconds());
        
        return timeSinceLastExecution.compareTo(requiredDelay) < 0;
    }
    
    private void executeArbitrageCycle(TaskDto task) {
        log.info("Executing arbitrage cycle for task: {}", task.getId());
        
        // 1. Scan for arbitrage opportunities (with caching)
        String cacheKey = getCacheKey(task);
        ArbitrageScanResponse scanResponse;
        
        CachedScanResult cached = scanCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            scanResponse = cached.getResponse();
            log.debug("Using cached scan results for task {} (age: {}s)", 
                    task.getId(), Duration.between(cached.getTimestamp(), Instant.now()).getSeconds());
        } else {
            ArbitrageScanRequest scanRequest = new ArbitrageScanRequest();
            scanRequest.setBaseAsset(task.getBaseAsset());
            scanRequest.setMaxAssets(task.getMaxAssets());
            scanRequest.setChainLength(task.getChainLength());
            
            // Adaptive profit threshold based on recent performance
            double adaptiveMinProfit = getAdaptiveMinProfit(task);
            scanRequest.setMinProfitPercent(adaptiveMinProfit);
            scanRequest.setReasoning("Automated task execution: " + task.getId());
            
            scanResponse = arbitrageTools.scanForArbitrage(scanRequest);
            
            // Cache the result
            scanCache.put(cacheKey, new CachedScanResult(scanResponse, Instant.now()));
            
            if (adaptiveMinProfit > task.getMinProfitPercent()) {
                log.info("Using adaptive profit threshold {}% (base: {}%) for task {} due to recent losses",
                        adaptiveMinProfit, task.getMinProfitPercent(), task.getId());
            }
        }
        
        if (scanResponse.getChains() == null || scanResponse.getChains().isEmpty()) {
            log.debug("No profitable chains found for task {}", task.getId());
            return;
        }
        
        // 2. Find suitable chain (filtering blacklisted and low-profit chains)
        ArbitrageScanResponse.ArbitrageChainDto selectedChain = null;
        for (ArbitrageScanResponse.ArbitrageChainDto chain : scanResponse.getChains()) {
            // Circuit Breaker: Skip blacklisted chains
            if (task.getEnableCircuitBreaker() != null && task.getEnableCircuitBreaker() && isChainBlacklisted(chain.getId())) {
                log.debug("Skipping blacklisted chain {} for task {}", chain.getId(), task.getId());
                continue;
            }
            
            // Slippage Protection: Check if profit is safe
            double safeThreshold = task.getMinProfitPercent() + (task.getSlippageTolerance() != null ? task.getSlippageTolerance() : 1.0);
            if (chain.getProfitPercent() < safeThreshold) {
                log.debug("Chain {} profit {}% below safe threshold {}%, skipping",
                        chain.getId(), chain.getProfitPercent(), safeThreshold);
                continue;
            }
            
            selectedChain = chain;
            break;
        }
        
        if (selectedChain == null) {
            log.debug("No suitable chains found for task {} after filtering", task.getId());
            return;
        }
        
        // 3. Calculate order amount with smart sizing
        double orderAmount = calculateOrderAmount(task, selectedChain);
        
        if (orderAmount < selectedChain.getMinRequiredAmount()) {
            log.warn("Insufficient budget for task {}: required={}, available={}", 
                    task.getId(), selectedChain.getMinRequiredAmount(), task.getCurrentBudget());
            return;
        }
        
        // 4. Execute the chain
        try {
            Map<String, Object> result = arbitrageTools.executeArbitrageChain(
                    selectedChain.getId(), 
                    orderAmount
            );
            
            // Create execution record
            ArbitrageExecution execution = createExecutionFromResult(
                    task, 
                    selectedChain, 
                    result, 
                    orderAmount
            );
            
            // Update chain statistics
            updateChainStats(selectedChain.getId(), execution, selectedChain.getProfitPercent());
            
            // Circuit Breaker: Reset failure count on success
            if (execution.getStatus().equals("COMPLETED") && execution.getProfitAmount() >= 0) {
                chainFailureCount.remove(selectedChain.getId());
            } else {
                recordChainFailure(selectedChain.getId());
            }
            
            // Add execution to task
            taskService.addExecution(task.getId(), execution);
            
            log.info("Successfully executed chain for task {}: profit={} ({}%), expected={}%, slippage={}%", 
                    task.getId(), execution.getProfitAmount(), execution.getProfitPercent(),
                    execution.getExpectedProfitPercent(), 
                    execution.getExpectedProfitPercent() - execution.getProfitPercent());
            
        } catch (Exception e) {
            log.error("Failed to execute chain for task {}: {}", task.getId(), e.getMessage());
            
            // Circuit Breaker: Record failure
            recordChainFailure(selectedChain.getId());
            
            // Record failed execution
            ArbitrageExecution failedExecution = ArbitrageExecution.builder()
                    .id(UUID.randomUUID().toString())
                    .chainId(selectedChain.getId())
                    .chain(toArbitrageChain(selectedChain))
                    .timestamp(Instant.now())
                    .initialAmount(orderAmount)
                    .finalAmount(orderAmount)
                    .profitAmount(0.0)
                    .profitPercent(0.0)
                    .expectedProfitPercent(selectedChain.getProfitPercent())
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
            
            taskService.addExecution(task.getId(), failedExecution);
            
            // Update stats for failed execution
            updateChainStats(selectedChain.getId(), failedExecution, selectedChain.getProfitPercent());
        }
    }
    
    private ArbitrageExecution createExecutionFromResult(
            TaskDto task,
            ArbitrageScanResponse.ArbitrageChainDto chainDto,
            Map<String, Object> result,
            double initialAmount) {
        
        String status = (String) result.get("status");
        Double finalAmount = result.containsKey("finalAmount") ? 
                ((Number) result.get("finalAmount")).doubleValue() : initialAmount;
        Double profitPercent = result.containsKey("profitPercent") ?
                ((Number) result.get("profitPercent")).doubleValue() : 0.0;
        
        double profitAmount = finalAmount - initialAmount;
        
        return ArbitrageExecution.builder()
                .id(UUID.randomUUID().toString())
                .chainId(chainDto.getId())
                .chain(toArbitrageChain(chainDto))
                .timestamp(Instant.now())
                .initialAmount(initialAmount)
                .finalAmount(finalAmount)
                .profitAmount(profitAmount)
                .profitPercent(profitPercent)
                .expectedProfitPercent(chainDto.getProfitPercent()) // Save predicted profit
                .status(status != null && status.equals("COMPLETED") ? "COMPLETED" : "FAILED")
                .errorMessage(result.containsKey("errorMessage") ? 
                        (String) result.get("errorMessage") : null)
                .build();
    }
    
    private ArbitrageChain toArbitrageChain(ArbitrageScanResponse.ArbitrageChainDto dto) {
        return ArbitrageChain.builder()
                .id(dto.getId())
                .baseAsset(dto.getBaseAsset())
                .steps(dto.getSteps())
                .profitPercent(dto.getProfitPercent())
                .minRequiredAmount(dto.getMinRequiredAmount())
                .timestamp(Instant.parse(dto.getTimestamp()))
                .build();
    }
    
    // ========== Circuit Breaker Methods ==========
    
    private boolean isChainBlacklisted(String chainId) {
        Instant blacklistUntil = chainBlacklist.get(chainId);
        if (blacklistUntil != null && Instant.now().isBefore(blacklistUntil)) {
            return true;
        }
        chainBlacklist.remove(chainId);
        return false;
    }
    
    private void recordChainFailure(String chainId) {
        int failures = chainFailureCount.getOrDefault(chainId, 0) + 1;
        chainFailureCount.put(chainId, failures);
        
        if (failures >= FAILURE_THRESHOLD) {
            Instant blacklistUntil = Instant.now().plus(Duration.ofMinutes(BLACKLIST_DURATION_MINUTES));
            chainBlacklist.put(chainId, blacklistUntil);
            log.warn("Chain {} blacklisted for {} minutes after {} consecutive failures",
                    chainId, BLACKLIST_DURATION_MINUTES, failures);
            chainFailureCount.remove(chainId);
        }
    }
    
    // ========== Chain Statistics Methods ==========
    
    private void updateChainStats(String chainId, ArbitrageExecution execution, double expectedProfit) {
        ChainStats stats = chainStatsMap.computeIfAbsent(chainId, k -> new ChainStats());
        stats.totalExecutions++;
        
        if (execution.getStatus().equals("COMPLETED") && execution.getProfitAmount() >= 0) {
            stats.successfulExecutions++;
        }
        
        double slippage = expectedProfit - execution.getProfitPercent();
        stats.totalSlippage += Math.abs(slippage);
    }
    
    private double getChainSuccessRate(String chainId) {
        ChainStats stats = chainStatsMap.get(chainId);
        return stats != null ? stats.getSuccessRate() : 0.0;
    }
    
    // ========== Smart Order Sizing ==========
    
    private double calculateOrderAmount(TaskDto task, ArbitrageScanResponse.ArbitrageChainDto chain) {
        double currentBudget = task.getCurrentBudget();
        double minRequired = chain.getMinRequiredAmount();
        
        // If smart sizing disabled, use min of required or full budget
        if (task.getEnableSmartSizing() == null || !task.getEnableSmartSizing()) {
            return Math.min(minRequired, currentBudget);
        }
        
        // Maximum per trade: 20% of current budget
        double maxPerTrade = currentBudget * 0.2;
        
        // Get chain success rate
        ChainStats stats = chainStatsMap.get(chain.getId());
        
        // For new or unreliable chains, use minimum amount
        if (stats == null || stats.getTotalExecutions() < 3 || stats.getSuccessRate() < 0.5) {
            double amount = Math.min(minRequired, maxPerTrade);
            log.debug("Using minimum amount {} for new/untested chain {}", amount, chain.getId());
            return amount;
        }
        
        // For proven chains, allow up to 20% of budget
        double amount = Math.min(maxPerTrade, currentBudget);
        log.debug("Using smart-sized amount {} for proven chain {} (success rate: {}%)",
                amount, chain.getId(), stats.getSuccessRate() * 100);
        return amount;
    }
    
    // ========== Emergency Stop Methods ==========
    
    private boolean shouldEmergencyStop(TaskDto task) {
        // Check total loss percentage
        double lossPercent = (task.getTotalLoss() / task.getBudget()) * 100;
        if (lossPercent > 5.0) {
            return true;
        }
        
        // Check net profit (total loss - total profit)
        double netProfitPercent = ((task.getTotalProfit() - task.getTotalLoss()) / task.getBudget()) * 100;
        if (netProfitPercent < -2.0) {
            return true;
        }
        
        // Check consecutive losses
        if (task.getConsecutiveLosses() != null && task.getConsecutiveLosses() >= 5) {
            return true;
        }
        
        return false;
    }
    
    private String getEmergencyStopReason(TaskDto task) {
        double lossPercent = (task.getTotalLoss() / task.getBudget()) * 100;
        double netProfitPercent = ((task.getTotalProfit() - task.getTotalLoss()) / task.getBudget()) * 100;
        
        if (lossPercent > 5.0) {
            return String.format("Total loss %.2f%% exceeds 5%% threshold", lossPercent);
        }
        
        if (netProfitPercent < -2.0) {
            return String.format("Net profit %.2f%% below -2%% threshold", netProfitPercent);
        }
        
        if (task.getConsecutiveLosses() != null && task.getConsecutiveLosses() >= 5) {
            return String.format("%d consecutive losses", task.getConsecutiveLosses());
        }
        
        return "Unknown reason";
    }
    
    // ========== Scan Caching Methods ==========
    
    private String getCacheKey(TaskDto task) {
        return String.format("%s_%d_%d_%.2f",
                task.getBaseAsset(),
                task.getMaxAssets(),
                task.getChainLength(),
                task.getMinProfitPercent());
    }
    
    // ========== Adaptive Profit Threshold ==========
    
    private double getAdaptiveMinProfit(TaskDto task) {
        double baseProfit = task.getMinProfitPercent();
        
        // Check recent losses
        int recentLosses = task.getConsecutiveLosses() != null ? task.getConsecutiveLosses() : 0;
        
        // Increase threshold by 0.5% for each recent loss (max +2.0%)
        double increase = Math.min(recentLosses * 0.5, 2.0);
        
        return baseProfit + increase;
    }
}

