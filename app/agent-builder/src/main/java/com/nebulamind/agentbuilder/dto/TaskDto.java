package com.nebulamind.agentbuilder.dto;

import com.nebulamind.agentbuilder.domain.TaskStatus;
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
public class TaskDto {
    
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
    private List<ArbitrageExecutionDto> executions = new ArrayList<>();
}

