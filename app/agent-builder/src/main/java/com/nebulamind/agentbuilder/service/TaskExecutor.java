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
public class TaskExecutor {
    
    private final TaskService taskService;
    private final ArbitrageTools arbitrageTools;
    private final Map<String, Instant> lastExecutionTime = new ConcurrentHashMap<>();
    
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
        
        // 1. Scan for arbitrage opportunities
        ArbitrageScanRequest scanRequest = new ArbitrageScanRequest();
        scanRequest.setBaseAsset(task.getBaseAsset());
        scanRequest.setMaxAssets(task.getMaxAssets());
        scanRequest.setChainLength(task.getChainLength());
        scanRequest.setMinProfitPercent(task.getMinProfitPercent());
        scanRequest.setReasoning("Automated task execution: " + task.getId());
        
        ArbitrageScanResponse scanResponse = arbitrageTools.scanForArbitrage(scanRequest);
        
        if (scanResponse.getChains() == null || scanResponse.getChains().isEmpty()) {
            log.debug("No profitable chains found for task {}", task.getId());
            return;
        }
        
        // 2. Execute the most profitable chain
        ArbitrageScanResponse.ArbitrageChainDto bestChain = scanResponse.getChains().get(0);
        
        // Check if we have enough budget
        double requiredAmount = Math.min(bestChain.getMinRequiredAmount(), task.getCurrentBudget());
        
        if (requiredAmount < bestChain.getMinRequiredAmount()) {
            log.warn("Insufficient budget for task {}: required={}, available={}", 
                    task.getId(), bestChain.getMinRequiredAmount(), task.getCurrentBudget());
            return;
        }
        
        // Execute the chain
        try {
            Map<String, Object> result = arbitrageTools.executeArbitrageChain(
                    bestChain.getId(), 
                    requiredAmount
            );
            
            // Create execution record
            ArbitrageExecution execution = createExecutionFromResult(
                    task, 
                    bestChain, 
                    result, 
                    requiredAmount
            );
            
            // Add execution to task
            taskService.addExecution(task.getId(), execution);
            
            log.info("Successfully executed chain for task {}: profit={} ({}%)", 
                    task.getId(), execution.getProfitAmount(), execution.getProfitPercent());
            
        } catch (Exception e) {
            log.error("Failed to execute chain for task {}: {}", task.getId(), e.getMessage());
            
            // Record failed execution
            ArbitrageExecution failedExecution = ArbitrageExecution.builder()
                    .id(UUID.randomUUID().toString())
                    .chainId(bestChain.getId())
                    .chain(toArbitrageChain(bestChain))
                    .timestamp(Instant.now())
                    .initialAmount(requiredAmount)
                    .finalAmount(requiredAmount)
                    .profitAmount(0.0)
                    .profitPercent(0.0)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
            
            taskService.addExecution(task.getId(), failedExecution);
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
}

