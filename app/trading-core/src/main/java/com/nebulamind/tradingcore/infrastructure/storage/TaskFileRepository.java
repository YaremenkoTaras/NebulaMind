package com.nebulamind.tradingcore.infrastructure.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File-based repository for ArbitrageTasks
 * Stores each task as a separate JSON file
 */
@Slf4j
@Repository
public class TaskFileRepository {
    
    private final Path storageDir;
    private final ObjectMapper objectMapper;
    
    public TaskFileRepository() {
        this.storageDir = Paths.get(System.getProperty("user.home"), ".nebulamind", "tasks");
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        
        // Create storage directory if not exists
        try {
            Files.createDirectories(storageDir);
            log.info("Task storage directory: {}", storageDir);
        } catch (IOException e) {
            log.error("Failed to create storage directory", e);
            throw new RuntimeException("Failed to initialize task storage", e);
        }
    }
    
    /**
     * Save task to file
     */
    public void save(ArbitrageTask task) {
        Path taskFile = getTaskFile(task.getId());
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(taskFile.toFile(), task);
            log.debug("Saved task: {}", task.getId());
        } catch (IOException e) {
            log.error("Failed to save task: {}", task.getId(), e);
            throw new RuntimeException("Failed to save task", e);
        }
    }
    
    /**
     * Find task by ID
     */
    public Optional<ArbitrageTask> findById(String id) {
        Path taskFile = getTaskFile(id);
        
        if (!Files.exists(taskFile)) {
            return Optional.empty();
        }
        
        try {
            ArbitrageTask task = objectMapper.readValue(taskFile.toFile(), ArbitrageTask.class);
            return Optional.of(task);
        } catch (IOException e) {
            log.error("Failed to read task: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Find all tasks
     */
    public List<ArbitrageTask> findAll() {
        try (Stream<Path> paths = Files.list(storageDir)) {
            return paths
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(this::readTaskFromFile)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list tasks", e);
            return List.of();
        }
    }
    
    /**
     * Find tasks by status
     */
    public List<ArbitrageTask> findByStatus(ArbitrageTask.TaskStatus status) {
        return findAll().stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete task by ID
     */
    public void deleteById(String id) {
        Path taskFile = getTaskFile(id);
        try {
            Files.deleteIfExists(taskFile);
            log.debug("Deleted task: {}", id);
        } catch (IOException e) {
            log.error("Failed to delete task: {}", id, e);
            throw new RuntimeException("Failed to delete task", e);
        }
    }
    
    /**
     * Delete all tasks
     */
    public void deleteAll() {
        try (Stream<Path> paths = Files.list(storageDir)) {
            paths.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error("Failed to delete file: {}", path, e);
                        }
                    });
            log.info("Deleted all tasks");
        } catch (IOException e) {
            log.error("Failed to delete tasks", e);
            throw new RuntimeException("Failed to delete all tasks", e);
        }
    }
    
    // Helper methods
    
    private Path getTaskFile(String taskId) {
        return storageDir.resolve(taskId + ".json");
    }
    
    private Optional<ArbitrageTask> readTaskFromFile(Path path) {
        try {
            ArbitrageTask task = objectMapper.readValue(path.toFile(), ArbitrageTask.class);
            return Optional.of(task);
        } catch (IOException e) {
            log.error("Failed to read task from file: {}", path, e);
            return Optional.empty();
        }
    }
}

