package com.nebulamind.tradingcore.domain.model.arbitrage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Domain model for triangular arbitrage chain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrageChain {
    private String id;
    private String baseAsset;  // Валюта А, яка є в наявності
    private List<ArbitrageStep> steps;
    private double profitPercent;
    private double minRequiredBaseAmount;
    private Instant timestamp;
    private ChainStatus status;
    
    public enum ChainStatus {
        FOUND,          // Знайдено, але не виконується
        EXECUTING,      // В процесі виконання
        COMPLETED,      // Успішно виконано
        FAILED,         // Помилка виконання
        CANCELLED      // Скасовано користувачем
    }
    
    /**
     * Validate chain basic structure
     * 
     * @return true if chain is valid (circular and connected)
     */
    public boolean isValid() {
        if (steps == null || steps.isEmpty()) {
            return false;
        }
        
        // Перевірка що перша валюта співпадає з останньою
        String firstAsset = steps.get(0).getFromAsset();
        String lastAsset = steps.get(steps.size() - 1).getToAsset();
        if (!firstAsset.equals(lastAsset)) {
            return false;
        }
        
        // Перевірка що ланцюжок зв'язаний
        for (int i = 0; i < steps.size() - 1; i++) {
            if (!steps.get(i).getToAsset().equals(steps.get(i + 1).getFromAsset())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate chain with required number of steps
     * 
     * @param requiredSteps кількість кроків, яка має бути в ланцюжку
     * @return true if chain is valid and has exactly requiredSteps steps
     */
    public boolean isValid(int requiredSteps) {
        if (steps == null || steps.size() != requiredSteps) {
            return false;
        }
        
        return isValid();
    }
    
    /**
     * Get unique assets in chain
     */
    public List<String> getUniqueAssets() {
        return steps.stream()
                .flatMap(step -> List.of(step.getFromAsset(), step.getToAsset()).stream())
                .distinct()
                .toList();
    }
    
    /**
     * Calculate expected profit for amount
     */
    public double calculateProfit(double baseAmount) {
        if (!isValid()) {
            return 0.0;
        }
        
        double amount = baseAmount;
        for (ArbitrageStep step : steps) {
            amount = amount * step.getRate();
        }
        
        return amount - baseAmount;
    }
}
