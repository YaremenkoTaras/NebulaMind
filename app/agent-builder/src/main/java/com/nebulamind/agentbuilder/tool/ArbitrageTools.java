package com.nebulamind.agentbuilder.tool;

import com.nebulamind.agentbuilder.client.TradingCoreClient;
import com.nebulamind.agentbuilder.config.AgentProperties;
import com.nebulamind.agentbuilder.dto.ArbitrageScanRequest;
import com.nebulamind.agentbuilder.dto.ArbitrageScanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tools for arbitrage operations available to LLM agent
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArbitrageTools {
    
    private final AgentProperties properties;
    private final WebClient webClient;
    
    /**
     * Scan for arbitrage opportunities
     * 
     * This tool helps LLM agent find profitable arbitrage chains.
     * 
     * @param request Scan parameters
     * @return Scan results with profitable chains
     */
    public ArbitrageScanResponse scanForArbitrage(ArbitrageScanRequest request) {
        log.info("Agent tool: scanForArbitrage - baseAsset={}, maxAssets={}, chainLength={}, minProfit={}%",
                request.getBaseAsset(), request.getMaxAssets(), 
                request.getChainLength(), request.getMinProfitPercent());
        
        // Validate request
        validateScanRequest(request);
        
        // Call trading core API to find chains
        String coreApiUrl = properties.getCoreApi().getBaseUrl() + "/api/core/arbitrage/chains/find";
        
        Map<String, Object> requestBody = Map.of(
                "baseAsset", request.getBaseAsset(),
                "maxAssets", request.getMaxAssets(),
                "chainLength", request.getChainLength(),
                "minProfitPercent", request.getMinProfitPercent()
        );
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chains = (List<Map<String, Object>>) (Object) webClient.post()
                .uri(coreApiUrl)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();
        
        // Convert to response format
        List<ArbitrageScanResponse.ArbitrageChainDto> chainDtos = new ArrayList<>();
        if (chains != null) {
            for (Map<String, Object> chain : chains) {
                List<Map<String, Object>> steps = (List<Map<String, Object>>) chain.get("steps");
                List<String> stepSymbols = steps.stream()
                        .map(step -> (String) step.get("symbol"))
                        .collect(Collectors.toList());
                
                ArbitrageScanResponse.ArbitrageChainDto dto = ArbitrageScanResponse.ArbitrageChainDto.builder()
                        .id((String) chain.get("id"))
                        .baseAsset((String) chain.get("baseAsset"))
                        .steps(stepSymbols)
                        .profitPercent(((Number) chain.get("profitPercent")).doubleValue())
                        .minRequiredAmount(((Number) chain.get("minRequiredBaseAmount")).doubleValue())
                        .timestamp((String) chain.get("timestamp"))
                        .build();
                
                chainDtos.add(dto);
            }
        }
        
        // Get analyzed assets
        List<String> analyzedAssets = getAnalyzedAssets();
        
        // Build summary
        double bestProfit = chainDtos.isEmpty() ? 0.0 : 
                chainDtos.stream()
                        .mapToDouble(ArbitrageScanResponse.ArbitrageChainDto::getProfitPercent)
                        .max()
                        .orElse(0.0);
        
        ArbitrageScanResponse.ScanSummary summary = ArbitrageScanResponse.ScanSummary.builder()
                .totalChainsFound(chainDtos.size())
                .profitableChainsFound(chainDtos.size())
                .bestProfitPercent(bestProfit)
                .analyzedAssets(analyzedAssets)
                .build();
        
        ArbitrageScanResponse response = ArbitrageScanResponse.builder()
                .chains(chainDtos)
                .summary(summary)
                .build();
        
        log.info("Scan completed: found {} profitable chains, best profit: {}%", 
                chainDtos.size(), bestProfit);
        
        return response;
    }
    
    /**
     * Get list of available assets for arbitrage
     * 
     * @return List of available asset symbols
     */
    public List<String> getAvailableAssets() {
        log.info("Agent tool: getAvailableAssets");
        
        String coreApiUrl = properties.getCoreApi().getBaseUrl() + "/api/core/arbitrage/assets";
        
        @SuppressWarnings("unchecked")
        java.util.Set<String> assets = webClient.get()
                .uri(coreApiUrl)
                .retrieve()
                .bodyToMono(java.util.Set.class)
                .block();
        
        log.info("Available assets: {} items", assets != null ? assets.size() : 0);
        
        return assets != null ? new ArrayList<>(assets) : List.of();
    }
    
    /**
     * Execute arbitrage chain
     * 
     * @param chainId Chain ID to execute
     * @param baseAmount Amount to trade
     * @return Execution result
     */
    public Map<String, Object> executeArbitrageChain(String chainId, double baseAmount) {
        log.info("Agent tool: executeArbitrageChain - chainId={}, baseAmount={}", chainId, baseAmount);
        
        // Validate
        if (baseAmount <= 0) {
            throw new IllegalArgumentException("Base amount must be positive");
        }
        
        if (baseAmount < 10.0) {
            log.warn("Base amount {} is less than recommended minimum (10.0)", baseAmount);
        }
        
        // Call trading core API to execute
        String coreApiUrl = properties.getCoreApi().getBaseUrl() + 
                "/api/core/arbitrage/chains/" + chainId + "/execute";
        
        // Create request body matching trading-core's ExecuteChainRequest DTO
        Map<String, Object> requestBody = Map.of("baseAmount", baseAmount);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = webClient.post()
                .uri(coreApiUrl)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        
        log.info("Chain execution result: {}", result);
        
        return result;
    }
    
    /**
     * Validate scan request
     */
    private void validateScanRequest(ArbitrageScanRequest request) {
        if (request.getBaseAsset() == null || request.getBaseAsset().isEmpty()) {
            throw new IllegalArgumentException("Base asset is required");
        }
        
        if (request.getMaxAssets() < 3) {
            throw new IllegalArgumentException("Max assets must be at least 3 for triangular arbitrage");
        }
        
        if (request.getChainLength() < 3) {
            throw new IllegalArgumentException("Chain length must be at least 3 for triangular arbitrage");
        }
        
        if (request.getMinProfitPercent() < 0) {
            throw new IllegalArgumentException("Min profit percent cannot be negative");
        }
    }
    
    /**
     * Get list of analyzed assets (mock implementation)
     */
    private List<String> getAnalyzedAssets() {
        // In real implementation, this would be returned from the API
        return List.of("BTC", "ETH", "BNB", "SOL", "ADA", "XRP", "DOT", "MATIC", "DOGE", "LTC");
    }
}

