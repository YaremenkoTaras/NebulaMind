package com.nebulamind.agentbuilder.api;

import com.nebulamind.agentbuilder.dto.ArbitrageScanRequest;
import com.nebulamind.agentbuilder.dto.ArbitrageScanResponse;
import com.nebulamind.agentbuilder.tool.ArbitrageTools;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for arbitrage tools (used by LLM agent)
 */
@RestController
@RequestMapping("/api/agent/arbitrage")
@RequiredArgsConstructor
@Slf4j
public class ArbitrageToolsController {
    
    private final ArbitrageTools arbitrageTools;
    
    /**
     * Scan for arbitrage opportunities
     * 
     * LLM agent calls this to find profitable arbitrage chains
     */
    @PostMapping("/scan")
    public ResponseEntity<ArbitrageScanResponse> scanForArbitrage(
            @Valid @RequestBody ArbitrageScanRequest request) {
        log.info("POST /api/agent/arbitrage/scan: {}", request);
        
        ArbitrageScanResponse response = arbitrageTools.scanForArbitrage(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get available assets for arbitrage
     */
    @GetMapping("/assets")
    public ResponseEntity<List<String>> getAvailableAssets() {
        log.info("GET /api/agent/arbitrage/assets");
        
        List<String> assets = arbitrageTools.getAvailableAssets();
        
        return ResponseEntity.ok(assets);
    }
    
    /**
     * Execute arbitrage chain
     * 
     * LLM agent calls this after user approval to execute the chain
     */
    @PostMapping("/chains/{chainId}/execute")
    public ResponseEntity<Map<String, Object>> executeChain(
            @PathVariable String chainId,
            @RequestParam(required = false) Double baseAmount,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        
        // Support both query param and request body
        double amount = baseAmount != null ? baseAmount : 
                (requestBody != null && requestBody.containsKey("baseAmount") ? 
                        ((Number) requestBody.get("baseAmount")).doubleValue() : 0.0);
        
        log.info("POST /api/agent/arbitrage/chains/{}/execute with baseAmount={}", chainId, amount);
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Base amount must be positive");
        }
        
        Map<String, Object> result = arbitrageTools.executeArbitrageChain(chainId, amount);
        
        return ResponseEntity.ok(result);
    }
}

