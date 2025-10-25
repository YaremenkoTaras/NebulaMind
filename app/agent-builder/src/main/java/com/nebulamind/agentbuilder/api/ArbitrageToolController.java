package com.nebulamind.agentbuilder.api;

import com.nebulamind.agentbuilder.tool.arbitrage.ArbitrageAnalysisTool;
import com.nebulamind.agentbuilder.tool.arbitrage.dto.AnalyzeMarketsRequest;
import com.nebulamind.agentbuilder.tool.arbitrage.dto.AnalyzeMarketsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for arbitrage analysis tools
 */
@RestController
@RequestMapping("/api/agent/tools/arbitrage")
@RequiredArgsConstructor
@Slf4j
public class ArbitrageToolController {

    private final ArbitrageAnalysisTool arbitrageAnalysisTool;

    /**
     * Analyze markets for arbitrage opportunities
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeMarketsResponse> analyzeMarkets(@RequestBody AnalyzeMarketsRequest request) {
        log.info("POST /api/agent/tools/arbitrage/analyze: {}", request);
        
        AnalyzeMarketsResponse response = arbitrageAnalysisTool.analyzeMarkets(request);
        return ResponseEntity.ok(response);
    }
}
