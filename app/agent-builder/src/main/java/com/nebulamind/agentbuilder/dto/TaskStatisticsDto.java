package com.nebulamind.agentbuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatisticsDto {
    
    private TaskDto task;
    private List<ArbitrageExecutionDto> profitableExecutions;
    private List<ArbitrageExecutionDto> lossExecutions;
    private Double totalProfit;
    private Double totalLoss;
    private Double netProfit;
}

