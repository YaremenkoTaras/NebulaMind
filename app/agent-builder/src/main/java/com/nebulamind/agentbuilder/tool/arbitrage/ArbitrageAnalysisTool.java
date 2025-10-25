package com.nebulamind.agentbuilder.tool.arbitrage;

import com.nebulamind.agentbuilder.client.TradingCoreClient;
import com.nebulamind.agentbuilder.config.AgentProperties;
import com.nebulamind.agentbuilder.tool.arbitrage.dto.AnalyzeMarketsRequest;
import com.nebulamind.agentbuilder.tool.arbitrage.dto.AnalyzeMarketsResponse;
import com.nebulamind.agentbuilder.tool.arbitrage.dto.MarketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LLM tool for analyzing trading pairs and suggesting arbitrage opportunities
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArbitrageAnalysisTool {

    private final TradingCoreClient tradingCoreClient;
    private final AgentProperties properties;

    /**
     * Analyze markets and suggest arbitrage opportunities
     * 
     * @param request Analysis request with parameters
     * @return Analysis response with suggestions
     */
    public AnalyzeMarketsResponse analyzeMarkets(AnalyzeMarketsRequest request) {
        log.info("Analyzing markets for arbitrage: {}", request);
        
        // Get available pairs from trading core
        List<MarketInfo> markets = tradingCoreClient.getMarketInfo();
        
        // Build prompt for LLM
        String prompt = buildPrompt(request, markets);
        
        // Call LLM for analysis
        String llmResponse = callLLM(prompt);
        
        // Parse response and validate suggestions
        return parseAndValidateResponse(llmResponse, markets);
    }
    
    private String buildPrompt(AnalyzeMarketsRequest request, List<MarketInfo> markets) {
        StringBuilder prompt = new StringBuilder();
        
        // System context
        prompt.append("You are a crypto trading expert analyzing markets for arbitrage opportunities.\n\n");
        
        // Task description
        prompt.append(String.format("""
            Task: Find profitable arbitrage chains with the following requirements:
            - Base asset: %s (must be first and last in chain)
            - Chain length: %d steps
            - Maximum assets to consider: %d
            - Focus on most liquid pairs
            
            Available markets:
            """,
            request.getBaseAsset(),
            request.getChainLength(),
            request.getMaxAssets()
        ));
        
        // Market data
        markets.forEach(market -> {
            prompt.append(String.format("""
                %s:
                - 24h volume: %.2f %s
                - Price: %.8f
                - Min qty: %.8f
                - Max qty: %.8f
                """,
                market.getSymbol(),
                market.getVolume24h(),
                market.getQuoteAsset(),
                market.getLastPrice(),
                market.getMinQty(),
                market.getMaxQty()
            ));
        });
        
        // Instructions
        prompt.append("""
            
            Please analyze the markets and:
            1. Select most liquid pairs based on 24h volume
            2. Find potential arbitrage chains that:
               - Start and end with the base asset
               - Have exactly the required chain length
               - Use only the most liquid pairs
               - Consider price impact and trading fees
            3. Sort suggestions by potential profit
            4. Include reasoning for each suggestion
            
            Format response as JSON with:
            {
              "suggestions": [
                {
                  "steps": ["BTCUSDT", "ETHBTC", "ETHUSDT"],
                  "reasoning": "High volume pairs with tight spreads...",
                  "estimatedProfitPercent": 0.5
                }
              ]
            }
            """);
        
        return prompt.toString();
    }
    
    private String callLLM(String prompt) {
        // TODO: Implement actual LLM call
        // For now return mock response
        return """
            {
              "suggestions": [
                {
                  "steps": ["BTCUSDT", "ETHBTC", "ETHUSDT"],
                  "reasoning": "These pairs have highest 24h volume and tight spreads",
                  "estimatedProfitPercent": 0.5
                }
              ]
            }
            """;
    }
    
    private AnalyzeMarketsResponse parseAndValidateResponse(String llmResponse, List<MarketInfo> markets) {
        // TODO: Parse JSON response
        // TODO: Validate suggested pairs exist
        // TODO: Check volumes and spreads
        return AnalyzeMarketsResponse.builder()
                .suggestions(List.of())
                .build();
    }
}
