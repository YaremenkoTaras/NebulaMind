package com.nebulamind.tradingcore.infrastructure.arbitrage;

import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;
import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageStep;
import com.nebulamind.tradingcore.domain.port.ArbitrageAnalyzer;
import com.nebulamind.tradingcore.domain.port.ExchangeGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Real implementation of ArbitrageAnalyzer for sandbox
 * 
 * Uses graph-based algorithm to find profitable arbitrage cycles
 */
@Component
@ConditionalOnProperty(name = "nebulamind.exchange.type", havingValue = "sandbox")
@RequiredArgsConstructor
@Slf4j
public class SandboxArbitrageAnalyzer implements ArbitrageAnalyzer {
    
    private final ExchangeGateway exchangeGateway;
    
    @Override
    public List<ArbitrageChain> findArbitrageOpportunities(
            String baseAsset,
            int maxAssets,
            int chainLength,
            double minProfitPercent
    ) {
        log.info("Finding arbitrage opportunities: baseAsset={}, maxAssets={}, chainLength={}, minProfit={}%",
                baseAsset, maxAssets, chainLength, minProfitPercent);
        
        // Build trading graph
        TradingGraph graph = buildTradingGraph();
        
        // Find top N most liquid assets
        Set<String> selectedAssets = selectTopAssets(graph, baseAsset, maxAssets);
        log.info("Selected {} assets for analysis: {}", selectedAssets.size(), selectedAssets);
        
        // Find all cycles of specified length starting from base asset
        List<ArbitrageChain> allChains = findCycles(graph, baseAsset, selectedAssets, chainLength);
        log.info("Found {} potential chains", allChains.size());
        
        // Filter profitable chains
        List<ArbitrageChain> profitableChains = allChains.stream()
                .filter(chain -> chain.getProfitPercent() >= minProfitPercent)
                .sorted(Comparator.comparingDouble(ArbitrageChain::getProfitPercent).reversed())
                .collect(Collectors.toList());
        
        log.info("Found {} profitable chains (>{}%)", profitableChains.size(), minProfitPercent);
        
        return profitableChains;
    }
    
    @Override
    public List<String> getAvailablePairs() {
        return exchangeGateway.getAvailablePairs();
    }
    
    @Override
    public double getCurrentRate(String symbol) {
        try {
            return exchangeGateway.getCurrentPrice(symbol);
        } catch (Exception e) {
            log.error("Failed to get rate for {}: {}", symbol, e.getMessage());
            return 0.0;
        }
    }
    
    @Override
    public boolean isPairActive(String symbol) {
        return exchangeGateway.isPairActive(symbol);
    }
    
    /**
     * Build trading graph from available pairs
     */
    private TradingGraph buildTradingGraph() {
        TradingGraph graph = new TradingGraph();
        
        List<String> pairs = getAvailablePairs();
        
        for (String symbol : pairs) {
            // Parse symbol (e.g., BTCUSDT -> BTC/USDT)
            TradingPair pair = parseSymbol(symbol);
            if (pair == null) {
                continue;
            }
            
            double rate = getCurrentRate(symbol);
            if (rate <= 0) {
                continue;
            }
            
            // Add edge for buying quote asset with base asset
            // E.g., BTCUSDT @ 50000 means: 1 USDT → 1/50000 BTC
            graph.addEdge(pair.quote, pair.base, symbol, rate, false);
            
            // Add reverse edge for selling
            // E.g., selling BTC for USDT: 1 BTC → 50000 USDT
            graph.addEdge(pair.base, pair.quote, symbol, 1.0 / rate, true);
        }
        
        log.info("Built trading graph with {} assets and {} pairs", 
                graph.getAssets().size(), pairs.size());
        
        return graph;
    }
    
    /**
     * Select top N most liquid assets
     */
    private Set<String> selectTopAssets(TradingGraph graph, String baseAsset, int maxAssets) {
        Set<String> assets = new HashSet<>();
        assets.add(baseAsset);
        
        // Get all assets connected to base asset
        Set<String> connectedAssets = graph.getConnectedAssets(baseAsset);
        
        // For now, just take first N-1 (in real implementation, sort by volume)
        assets.addAll(connectedAssets.stream()
                .limit(maxAssets - 1)
                .collect(Collectors.toSet()));
        
        return assets;
    }
    
    /**
     * Find all cycles of specified length starting from base asset
     */
    private List<ArbitrageChain> findCycles(
            TradingGraph graph,
            String startAsset,
            Set<String> allowedAssets,
            int targetLength
    ) {
        List<ArbitrageChain> chains = new ArrayList<>();
        List<ArbitrageStep> currentPath = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        dfs(graph, startAsset, startAsset, allowedAssets, targetLength, 
            currentPath, visited, chains, 1.0);
        
        return chains;
    }
    
    /**
     * DFS to find cycles
     */
    private void dfs(
            TradingGraph graph,
            String current,
            String target,
            Set<String> allowedAssets,
            int remainingSteps,
            List<ArbitrageStep> currentPath,
            Set<String> visited,
            List<ArbitrageChain> result,
            double accumulatedRate
    ) {
        // Base case: reached target with correct length
        if (remainingSteps == 0) {
            if (current.equals(target) && !currentPath.isEmpty()) {
                // Calculate profit
                double profitPercent = (accumulatedRate - 1.0) * 100.0;
                
                // Calculate minimum required base amount
                double minRequired = calculateMinRequiredAmount(currentPath);
                
                // Create chain
                ArbitrageChain chain = ArbitrageChain.builder()
                        .id(UUID.randomUUID().toString())
                        .baseAsset(target)
                        .steps(new ArrayList<>(currentPath))
                        .profitPercent(profitPercent)
                        .minRequiredBaseAmount(minRequired)
                        .timestamp(Instant.now())
                        .status(ArbitrageChain.ChainStatus.FOUND)
                        .build();
                
                result.add(chain);
            }
            return;
        }
        
        // Pruning: if current rate is already unprofitable, skip
        if (accumulatedRate < 0.5) {
            return;
        }
        
        // Explore neighbors
        List<TradingGraph.Edge> edges = graph.getEdges(current);
        for (TradingGraph.Edge edge : edges) {
            String nextAsset = edge.to;
            
            // Check if asset is allowed
            if (!allowedAssets.contains(nextAsset)) {
                continue;
            }
            
            // If not at target yet, don't revisit (except target at the end)
            if (remainingSteps > 1 && visited.contains(nextAsset)) {
                continue;
            }
            
            // Add to path
            ArbitrageStep step = ArbitrageStep.builder()
                    .fromAsset(current)
                    .toAsset(nextAsset)
                    .symbol(edge.symbol)
                    .rate(edge.rate)
                    .minQty(0.001) // TODO: Get from exchange
                    .maxQty(1000.0) // TODO: Get from exchange
                    .priceDecimals(8)
                    .qtyDecimals(8)
                    .build();
            
            currentPath.add(step);
            visited.add(current);
            
            // Recursive call
            dfs(graph, nextAsset, target, allowedAssets, remainingSteps - 1,
                currentPath, visited, result, accumulatedRate * edge.rate);
            
            // Backtrack
            currentPath.remove(currentPath.size() - 1);
            visited.remove(current);
        }
    }
    
    /**
     * Parse trading pair symbol
     */
    private TradingPair parseSymbol(String symbol) {
        // Common quote currencies
        String[] quotes = {"USDT", "BTC", "ETH", "BNB", "BUSD", "USD", "EUR"};
        
        for (String quote : quotes) {
            if (symbol.endsWith(quote)) {
                String base = symbol.substring(0, symbol.length() - quote.length());
                if (!base.isEmpty()) {
                    return new TradingPair(base, quote);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Trading pair representation
     */
    private static class TradingPair {
        String base;
        String quote;
        
        TradingPair(String base, String quote) {
            this.base = base;
            this.quote = quote;
        }
    }
    
    /**
     * Calculate minimum required base amount for a chain
     * Works backwards from the last step to find minimum starting amount
     */
    private double calculateMinRequiredAmount(List<ArbitrageStep> steps) {
        if (steps.isEmpty()) {
            return 10.0;
        }
        
        // Start from the end and work backwards
        // Find the maximum minQty requirement when converted to base currency
        double maxRequired = 10.0; // Default minimum
        
        for (int i = 0; i < steps.size(); i++) {
            ArbitrageStep step = steps.get(i);
            
            // Determine if this is a BUY or SELL
            boolean isBuy = step.getSymbol().startsWith(step.getToAsset());
            
            // Calculate what baseAmount would be needed for this step to have minQty
            double requiredForThisStep;
            if (isBuy) {
                // For BUY: baseAmount (quote) / rate = quantity (base)
                // So: baseAmount = minQty * rate
                requiredForThisStep = step.getMinQty() * step.getRate();
            } else {
                // For SELL: baseAmount (base) = quantity (base)
                requiredForThisStep = step.getMinQty();
            }
            
            // Propagate back through previous steps
            for (int j = i - 1; j >= 0; j--) {
                ArbitrageStep prevStep = steps.get(j);
                boolean prevIsBuy = prevStep.getSymbol().startsWith(prevStep.getToAsset());
                
                if (prevIsBuy) {
                    // Previous step BUYs: baseAmount / rate = output
                    // So: baseAmount = output * rate
                    requiredForThisStep = requiredForThisStep * prevStep.getRate();
                } else {
                    // Previous step SELLs: output = baseAmount * rate
                    // So: baseAmount = output / rate
                    requiredForThisStep = requiredForThisStep / prevStep.getRate();
                }
            }
            
            maxRequired = Math.max(maxRequired, requiredForThisStep);
        }
        
        // Round up to nearest 10
        return Math.ceil(maxRequired / 10.0) * 10.0;
    }
    
    /**
     * Trading graph representation
     */
    private static class TradingGraph {
        private final Map<String, List<Edge>> adjacencyList = new HashMap<>();
        
        void addEdge(String from, String to, String symbol, double rate, boolean isReverse) {
            adjacencyList.computeIfAbsent(from, k -> new ArrayList<>())
                    .add(new Edge(to, symbol, rate, isReverse));
        }
        
        List<Edge> getEdges(String asset) {
            return adjacencyList.getOrDefault(asset, List.of());
        }
        
        Set<String> getAssets() {
            return adjacencyList.keySet();
        }
        
        Set<String> getConnectedAssets(String asset) {
            return getEdges(asset).stream()
                    .map(edge -> edge.to)
                    .collect(Collectors.toSet());
        }
        
        static class Edge {
            String to;
            String symbol;
            double rate;
            boolean isReverse;
            
            Edge(String to, String symbol, double rate, boolean isReverse) {
                this.to = to;
                this.symbol = symbol;
                this.rate = rate;
                this.isReverse = isReverse;
            }
        }
    }
}

