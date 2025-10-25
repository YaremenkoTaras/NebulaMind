package com.nebulamind.agentbuilder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebulamind.agentbuilder.domain.*;
import com.nebulamind.agentbuilder.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskService {
    
    private final ObjectMapper objectMapper;
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final Path storageDir = Paths.get("data", "tasks");
    
    public TaskService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initializeStorage();
        loadTasksFromDisk();
    }
    
    private void initializeStorage() {
        try {
            Files.createDirectories(storageDir);
            log.info("Task storage initialized at: {}", storageDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create task storage directory", e);
        }
    }
    
    private void loadTasksFromDisk() {
        try {
            File dir = storageDir.toFile();
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
                if (files != null) {
                    for (File file : files) {
                        try {
                            Task task = objectMapper.readValue(file, Task.class);
                            tasks.put(task.getId(), task);
                            log.info("Loaded task from disk: {}", task.getId());
                        } catch (IOException e) {
                            log.error("Failed to load task from file: {}", file.getName(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load tasks from disk", e);
        }
    }
    
    private void saveTaskToDisk(Task task) {
        try {
            Path taskFile = storageDir.resolve(task.getId() + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(taskFile.toFile(), task);
            log.debug("Task saved to disk: {}", task.getId());
        } catch (IOException e) {
            log.error("Failed to save task to disk: {}", task.getId(), e);
        }
    }
    
    private void deleteTaskFromDisk(String taskId) {
        try {
            Path taskFile = storageDir.resolve(taskId + ".json");
            Files.deleteIfExists(taskFile);
            log.debug("Task deleted from disk: {}", taskId);
        } catch (IOException e) {
            log.error("Failed to delete task from disk: {}", taskId, e);
        }
    }
    
    public TaskDto createTask(TaskCreateRequest request) {
        Task task = Task.builder()
                .id(UUID.randomUUID().toString())
                .status(TaskStatus.PENDING)
                .baseAsset(request.getBaseAsset())
                .budget(request.getBudget())
                .currentBudget(request.getBudget())
                .executionTimeMinutes(request.getExecutionTimeMinutes())
                .delaySeconds(request.getDelaySeconds())
                .minProfitPercent(request.getMinProfitPercent())
                .maxAssets(request.getMaxAssets())
                .chainLength(request.getChainLength())
                .createdAt(Instant.now())
                .totalProfit(0.0)
                .totalLoss(0.0)
                .executionsCount(0)
                .executions(new ArrayList<>())
                .build();
        
        tasks.put(task.getId(), task);
        saveTaskToDisk(task);
        
        log.info("Created task: {}", task.getId());
        
        return toDto(task);
    }
    
    public List<TaskDto> getAllTasks() {
        return tasks.values().stream()
                .sorted(Comparator.comparing(Task::getCreatedAt).reversed())
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public TaskDto getTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        return toDto(task);
    }
    
    public TaskDto startTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        
        if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.STOPPED) {
            throw new IllegalStateException("Task cannot be started in current state: " + task.getStatus());
        }
        
        task.setStatus(TaskStatus.RUNNING);
        task.setStartedAt(Instant.now());
        saveTaskToDisk(task);
        
        log.info("Started task: {}", taskId);
        
        return toDto(task);
    }
    
    public TaskDto stopTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        
        if (task.getStatus() != TaskStatus.RUNNING) {
            throw new IllegalStateException("Task is not running: " + task.getStatus());
        }
        
        task.setStatus(TaskStatus.STOPPED);
        saveTaskToDisk(task);
        
        log.info("Stopped task: {}", taskId);
        
        return toDto(task);
    }
    
    public void deleteTask(String taskId) {
        Task task = tasks.remove(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        
        deleteTaskFromDisk(taskId);
        
        log.info("Deleted task: {}", taskId);
    }
    
    public TaskStatisticsDto getTaskStatistics(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        
        List<ArbitrageExecutionDto> profitableExecutions = task.getExecutions().stream()
                .filter(e -> e.getProfitAmount() > 0)
                .map(this::toExecutionDto)
                .collect(Collectors.toList());
        
        List<ArbitrageExecutionDto> lossExecutions = task.getExecutions().stream()
                .filter(e -> e.getProfitAmount() <= 0)
                .map(this::toExecutionDto)
                .collect(Collectors.toList());
        
        return TaskStatisticsDto.builder()
                .task(toDto(task))
                .profitableExecutions(profitableExecutions)
                .lossExecutions(lossExecutions)
                .totalProfit(task.getTotalProfit())
                .totalLoss(task.getTotalLoss())
                .netProfit(task.getTotalProfit() - task.getTotalLoss())
                .build();
    }
    
    public void addExecution(String taskId, ArbitrageExecution execution) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        
        task.addExecution(execution);
        saveTaskToDisk(task);
        
        log.info("Added execution to task {}: profit={}", taskId, execution.getProfitAmount());
    }
    
    public void completeTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            return;
        }
        
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(Instant.now());
        saveTaskToDisk(task);
        
        log.info("Completed task: {}", taskId);
    }
    
    public void failTask(String taskId, String errorMessage) {
        Task task = tasks.get(taskId);
        if (task == null) {
            return;
        }
        
        task.setStatus(TaskStatus.FAILED);
        task.setCompletedAt(Instant.now());
        saveTaskToDisk(task);
        
        log.error("Failed task {}: {}", taskId, errorMessage);
    }
    
    private TaskDto toDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .status(task.getStatus())
                .baseAsset(task.getBaseAsset())
                .budget(task.getBudget())
                .currentBudget(task.getCurrentBudget())
                .executionTimeMinutes(task.getExecutionTimeMinutes())
                .delaySeconds(task.getDelaySeconds())
                .minProfitPercent(task.getMinProfitPercent())
                .maxAssets(task.getMaxAssets())
                .chainLength(task.getChainLength())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .totalProfit(task.getTotalProfit())
                .totalLoss(task.getTotalLoss())
                .executionsCount(task.getExecutionsCount())
                .executions(task.getExecutions().stream()
                        .map(this::toExecutionDto)
                        .collect(Collectors.toList()))
                .build();
    }
    
    private ArbitrageExecutionDto toExecutionDto(ArbitrageExecution execution) {
        return ArbitrageExecutionDto.builder()
                .id(execution.getId())
                .chainId(execution.getChainId())
                .chain(toChainDto(execution.getChain()))
                .timestamp(execution.getTimestamp())
                .initialAmount(execution.getInitialAmount())
                .finalAmount(execution.getFinalAmount())
                .profitAmount(execution.getProfitAmount())
                .profitPercent(execution.getProfitPercent())
                .expectedProfitPercent(execution.getExpectedProfitPercent())
                .status(execution.getStatus())
                .errorMessage(execution.getErrorMessage())
                .build();
    }
    
    private ArbitrageChainDto toChainDto(ArbitrageChain chain) {
        return ArbitrageChainDto.builder()
                .id(chain.getId())
                .baseAsset(chain.getBaseAsset())
                .steps(chain.getSteps())
                .profitPercent(chain.getProfitPercent())
                .minRequiredAmount(chain.getMinRequiredAmount())
                .timestamp(chain.getTimestamp())
                .build();
    }
}

