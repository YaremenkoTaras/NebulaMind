package com.nebulamind.tradingcore.api;

import com.nebulamind.tradingcore.api.dto.arbitrage.ExecuteChainRequest;
import com.nebulamind.tradingcore.api.dto.arbitrage.FindChainsRequest;
import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;
import com.nebulamind.tradingcore.service.arbitrage.ArbitrageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for arbitrage operations
 */
@RestController
@RequestMapping("/api/core/arbitrage")
@RequiredArgsConstructor
@Slf4j
public class ArbitrageController {

    private final ArbitrageService arbitrageService;

    /**
     * Find profitable arbitrage chains
     */
    @PostMapping("/chains/find")
    public ResponseEntity<List<ArbitrageChain>> findChains(@Valid @RequestBody FindChainsRequest request) {
        log.info("POST /api/core/arbitrage/chains/find: {}", request);
        
        List<ArbitrageChain> chains = arbitrageService.findProfitableChains(
                request.getBaseAsset(),
                request.getMaxAssets(),
                request.getChainLength(),
                request.getMinProfitPercent()
        );
        
        return ResponseEntity.ok(chains);
    }

    /**
     * Execute arbitrage chain
     */
    @PostMapping("/chains/{chainId}/execute")
    public ResponseEntity<ArbitrageChain> executeChain(
            @PathVariable String chainId,
            @Valid @RequestBody ExecuteChainRequest request
    ) {
        log.info("POST /api/core/arbitrage/chains/{}/execute: {}", chainId, request);
        
        ArbitrageChain chain = arbitrageService.executeChain(chainId, request.getBaseAmount());
        return ResponseEntity.ok(chain);
    }

    /**
     * Cancel chain execution
     */
    @PostMapping("/chains/{chainId}/cancel")
    public ResponseEntity<Void> cancelChain(@PathVariable String chainId) {
        log.info("POST /api/core/arbitrage/chains/{}/cancel", chainId);
        
        arbitrageService.cancelChain(chainId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get chain status
     */
    @GetMapping("/chains/{chainId}")
    public ResponseEntity<ArbitrageChain> getChainStatus(@PathVariable String chainId) {
        log.info("GET /api/core/arbitrage/chains/{}", chainId);
        
        ArbitrageChain chain = arbitrageService.getChainStatus(chainId);
        if (chain == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chain);
    }
    
    /**
     * Get available trading pairs
     */
    @GetMapping("/pairs")
    public ResponseEntity<java.util.List<String>> getAvailablePairs() {
        log.info("GET /api/core/arbitrage/pairs");
        
        java.util.List<String> pairs = arbitrageService.getAvailablePairs();
        return ResponseEntity.ok(pairs);
    }
    
    /**
     * Get unique assets from available pairs
     */
    @GetMapping("/assets")
    public ResponseEntity<java.util.Set<String>> getAvailableAssets() {
        log.info("GET /api/core/arbitrage/assets");
        
        java.util.Set<String> assets = arbitrageService.getAvailableAssets();
        return ResponseEntity.ok(assets);
    }
}
