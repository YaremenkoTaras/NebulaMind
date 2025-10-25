package com.nebulamind.agentbuilder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebulamind.agentbuilder.config.AgentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * Service for LLM integration (OpenAI GPT)
 * 
 * Provides intelligent analysis for arbitrage opportunities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {
    
    private final AgentProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Use LLM to select most promising assets for arbitrage analysis
     * 
     * @param baseAsset Base asset to start/end with
     * @param availableAssets All available assets
     * @param maxAssets Maximum number to select
     * @param marketContext Optional market context
     * @return List of selected asset symbols
     */
    public List<String> selectOptimalAssets(
            String baseAsset,
            List<String> availableAssets,
            int maxAssets,
            String marketContext
    ) {
        log.info("LLM: Selecting optimal assets for arbitrage (base={}, max={})", baseAsset, maxAssets);
        
        // For now, return simple selection (top liquid assets)
        // TODO: Implement actual OpenAI API call
        
        // Prioritize major assets
        List<String> priority = Arrays.asList("BTC", "ETH", "BNB", "SOL", "ADA", 
                "XRP", "DOT", "MATIC", "AVAX", "LINK", "UNI", "LTC", "DOGE");
        
        List<String> selected = new ArrayList<>();
        selected.add(baseAsset); // Always include base
        
        // Add priority assets that are available
        for (String asset : priority) {
            if (selected.size() >= maxAssets) break;
            if (!selected.contains(asset) && availableAssets.contains(asset)) {
                selected.add(asset);
            }
        }
        
        // Fill remaining slots with other available assets
        for (String asset : availableAssets) {
            if (selected.size() >= maxAssets) break;
            if (!selected.contains(asset)) {
                selected.add(asset);
            }
        }
        
        log.info("LLM: Selected {} assets: {}", selected.size(), selected);
        return selected;
    }
    
    /**
     * Analyze market conditions and suggest optimal parameters
     * 
     * @param baseAsset Base asset
     * @param reasoning User's reasoning
     * @return Suggested parameters
     */
    public Map<String, Object> suggestParameters(String baseAsset, String reasoning) {
        log.info("LLM: Suggesting parameters for base={}", baseAsset);
        
        // Default suggestions
        Map<String, Object> suggestions = new HashMap<>();
        suggestions.put("maxAssets", 20);
        suggestions.put("chainLength", 3);
        suggestions.put("minProfitPercent", 0.5);
        suggestions.put("reasoning", "High liquidity majors with triangular path optimization");
        
        // TODO: Implement actual LLM analysis
        // - Analyze current market volatility
        // - Consider spread patterns
        // - Adjust parameters based on market conditions
        
        return suggestions;
    }
    
    /**
     * Generate reasoning for why a chain is profitable
     * 
     * @param steps Chain steps
     * @param profitPercent Profit percentage
     * @return Human-readable reasoning
     */
    public String explainChainProfitability(List<String> steps, double profitPercent) {
        // Simple template-based reasoning
        // TODO: Implement actual LLM explanation
        
        if (profitPercent >= 2.0) {
            return String.format(
                "Significant arbitrage opportunity (%.2f%%) due to price inefficiencies across %d trading pairs. " +
                "This indicates temporary market imbalance that can be exploited.",
                profitPercent, steps.size()
            );
        } else if (profitPercent >= 1.0) {
            return String.format(
                "Moderate arbitrage opportunity (%.2f%%) found through triangular path. " +
                "Profit margin is healthy enough to cover fees and slippage.",
                profitPercent
            );
        } else {
            return String.format(
                "Small arbitrage opportunity (%.2f%%) detected. " +
                "Best for low-latency execution with minimal fees.",
                profitPercent
            );
        }
    }
}

