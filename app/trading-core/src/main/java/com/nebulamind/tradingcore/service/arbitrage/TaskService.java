package com.nebulamind.tradingcore.service.arbitrage;

import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;
import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageTask;
import com.nebulamind.tradingcore.infrastructure.storage.TaskFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for managing arbitrage tasks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    
    private final TaskFileRepository taskRepository;
    private final ArbitrageService arbitrageService;
    
    // Track running tasks
    private final ConcurrentHashMap<String, CompletableFuture<Void>> runningTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
    
    /**
     * Create new task
     */
    public ArbitrageTask createTask(
            String baseAsset,
            int maxAssets,
            int chainLength,
            double minProfitPercent,
            double budget,
            int durationMinutes,
            int delaySeconds
    ) {
        ArbitrageTask task = ArbitrageTask.builder()
                .id(UUID.randomUUID().toString())
                .status(ArbitrageTask.TaskStatus.CREATED)
                .baseAsset(baseAsset)
                .maxAssets(maxAssets)
                .chainLength(chainLength)
                .minProfitPercent(minProfitPercent)
                .budget(budget)
                .durationMinutes(durationMinutes)
                .delaySeconds(delaySeconds)
                .currentBalance(budget)
                .totalProfit(0.0)
                .successfulTrades(0)
                .failedTrades(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        taskRepository.save(task);
        log.info("Created task: {}", task.getId());
        
        return task;
    }
    
    /**
     * Start task execution
     */
    public void startTask(String taskId) {
        ArbitrageTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        
        if (task.getStatus() != ArbitrageTask.TaskStatus.CREATED && 
            task.getStatus() != ArbitrageTask.TaskStatus.STOPPED) {
            throw new IllegalStateException("Task cannot be started. Current status: " + task.getStatus());
        }
        
        task.setStatus(ArbitrageTask.TaskStatus.RUNNING);
        task.setStartTime(Instant.now());
        task.setEndTime(Instant.now().plusSeconds(task.getDurationMinutes() * 60L));
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
        
        log.info("Starting task: {}", taskId);
        
        // Start async execution loop
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> executeTaskLoop(taskId), executorService);
        runningTasks.put(taskId, future);
    }
    
    /**
     * Stop task execution
     */
    public void stopTask(String taskId) {
        ArbitrageTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        
        task.setStatus(ArbitrageTask.TaskStatus.STOPPED);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
        
        // Cancel running future
        CompletableFuture<Void> future = runningTasks.remove(taskId);
        if (future != null) {
            future.cancel(true);
        }
        
        log.info("Stopped task: {}", taskId);
    }
    
    /**
     * Get task by ID
     */
    public ArbitrageTask getTask(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }
    
    /**
     * List all tasks
     */
    public List<ArbitrageTask> listTasks() {
        return taskRepository.findAll();
    }
    
    /**
     * List running tasks
     */
    public List<ArbitrageTask> listRunningTasks() {
        return taskRepository.findByStatus(ArbitrageTask.TaskStatus.RUNNING);
    }
    
    /**
     * Delete task
     */
    public void deleteTask(String taskId) {
        // Stop if running
        if (runningTasks.containsKey(taskId)) {
            stopTask(taskId);
        }
        
        taskRepository.deleteById(taskId);
        log.info("Deleted task: {}", taskId);
    }
    
    /**
     * Main task execution loop
     */
    private void executeTaskLoop(String taskId) {
        log.info("Task loop started: {}", taskId);
        
        try {
            while (true) {
                ArbitrageTask task = taskRepository.findById(taskId).orElse(null);
                if (task == null || !task.shouldContinue()) {
                    log.info("Task loop ending: {}", taskId);
                    if (task != null) {
                        task.setStatus(ArbitrageTask.TaskStatus.COMPLETED);
                        task.setUpdatedAt(Instant.now());
                        taskRepository.save(task);
                    }
                    break;
                }
                
                // Scan for profitable chains
                log.debug("Task {} - scanning for chains...", taskId);
                List<ArbitrageChain> chains = arbitrageService.findProfitableChains(
                        task.getBaseAsset(),
                        task.getMaxAssets(),
                        task.getChainLength(),
                        task.getMinProfitPercent()
                );
                
                if (chains.isEmpty()) {
                    log.debug("Task {} - no profitable chains found, sleeping {}s", taskId, task.getDelaySeconds());
                    Thread.sleep(task.getDelaySeconds() * 1000L);
                    continue;
                }
                
                // Execute best chain
                ArbitrageChain bestChain = chains.get(0);
                log.info("Task {} - executing chain {} with profit {}%", 
                        taskId, bestChain.getId(), bestChain.getProfitPercent());
                
                try {
                    ArbitrageChain result = arbitrageService.executeChain(bestChain.getId(), task.getCurrentBalance());
                    
                    // Record execution
                    ArbitrageTask.ExecutedChainRecord record = ArbitrageTask.ExecutedChainRecord.builder()
                            .chainId(result.getId())
                            .initialAmount(result.getInitialAmount())
                            .finalAmount(result.getFinalAmount())
                            .profit(result.getFinalAmount() - result.getInitialAmount())
                            .profitPercent(result.getProfitPercent())
                            .timestamp(Instant.now())
                            .status(result.getStatus().name())
                            .steps(result.getSteps().stream()
                                    .map(step -> step.getSymbol())
                                    .collect(Collectors.toList()))
                            .build();
                    
                    task.addExecutedChain(record);
                    taskRepository.save(task);
                    
                    log.info("Task {} - chain executed successfully, new balance: {}", 
                            taskId, task.getCurrentBalance());
                    
                } catch (Exception e) {
                    log.error("Task {} - chain execution failed", taskId, e);
                    task.setFailedTrades(task.getFailedTrades() + 1);
                    task.setUpdatedAt(Instant.now());
                    taskRepository.save(task);
                    
                    // Sleep on error
                    Thread.sleep(task.getDelaySeconds() * 1000L);
                }
                
                // Small delay between cycles
                Thread.sleep(1000);
            }
            
        } catch (InterruptedException e) {
            log.info("Task {} interrupted", taskId);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Task {} failed with error", taskId, e);
            ArbitrageTask task = taskRepository.findById(taskId).orElse(null);
            if (task != null) {
                task.setStatus(ArbitrageTask.TaskStatus.FAILED);
                task.setUpdatedAt(Instant.now());
                taskRepository.save(task);
            }
        } finally {
            runningTasks.remove(taskId);
        }
    }
    
    /**
     * Shutdown executor service
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

