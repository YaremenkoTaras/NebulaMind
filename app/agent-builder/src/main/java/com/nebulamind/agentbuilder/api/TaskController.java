package com.nebulamind.agentbuilder.api;

import com.nebulamind.agentbuilder.dto.TaskCreateRequest;
import com.nebulamind.agentbuilder.dto.TaskDto;
import com.nebulamind.agentbuilder.dto.TaskStatisticsDto;
import com.nebulamind.agentbuilder.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Task management
 */
@RestController
@RequestMapping("/api/agent/arbitrage/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * Create a new arbitrage task
     */
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskCreateRequest request) {
        log.info("POST /api/agent/arbitrage/tasks: {}", request);
        
        TaskDto task = taskService.createTask(request);
        
        return ResponseEntity.ok(task);
    }
    
    /**
     * Get all tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        log.info("GET /api/agent/arbitrage/tasks");
        
        List<TaskDto> tasks = taskService.getAllTasks();
        
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get task by ID
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> getTask(@PathVariable String taskId) {
        log.info("GET /api/agent/arbitrage/tasks/{}", taskId);
        
        TaskDto task = taskService.getTask(taskId);
        
        return ResponseEntity.ok(task);
    }
    
    /**
     * Start a task
     */
    @PostMapping("/{taskId}/start")
    public ResponseEntity<TaskDto> startTask(@PathVariable String taskId) {
        log.info("POST /api/agent/arbitrage/tasks/{}/start", taskId);
        
        TaskDto task = taskService.startTask(taskId);
        
        return ResponseEntity.ok(task);
    }
    
    /**
     * Stop a task
     */
    @PostMapping("/{taskId}/stop")
    public ResponseEntity<TaskDto> stopTask(@PathVariable String taskId) {
        log.info("POST /api/agent/arbitrage/tasks/{}/stop", taskId);
        
        TaskDto task = taskService.stopTask(taskId);
        
        return ResponseEntity.ok(task);
    }
    
    /**
     * Get task statistics
     */
    @GetMapping("/{taskId}/statistics")
    public ResponseEntity<TaskStatisticsDto> getTaskStatistics(@PathVariable String taskId) {
        log.info("GET /api/agent/arbitrage/tasks/{}/statistics", taskId);
        
        TaskStatisticsDto statistics = taskService.getTaskStatistics(taskId);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Delete a task
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        log.info("DELETE /api/agent/arbitrage/tasks/{}", taskId);
        
        taskService.deleteTask(taskId);
        
        return ResponseEntity.noContent().build();
    }
}

