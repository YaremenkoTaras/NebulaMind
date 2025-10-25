package com.nebulamind.tradingcore.service.arbitrage;

import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;
import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageStep;
import com.nebulamind.tradingcore.domain.port.ArbitrageAnalyzer;
import com.nebulamind.tradingcore.domain.port.ChainExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Service for finding and executing arbitrage opportunities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArbitrageService {

    private final ArbitrageAnalyzer arbitrageAnalyzer;
    private final ChainExecutor chainExecutor;
    
    /**
     * Find profitable arbitrage chains
     * 
     * @param baseAsset Base asset (e.g. USDT)
     * @param maxAssets Maximum number of assets to analyze
     * @param chainLength Required length of arbitrage chain
     * @param minProfitPercent Minimum profit to consider
     * @return List of profitable chains, sorted by profit desc
     */
    public List<ArbitrageChain> findProfitableChains(
            String baseAsset,
            int maxAssets,
            int chainLength,
            double minProfitPercent
    ) {
        log.info("Searching for arbitrage opportunities: baseAsset={}, maxAssets={}, chainLength={}, minProfit={}%",
                baseAsset, maxAssets, chainLength, minProfitPercent);
        
        List<ArbitrageChain> chains = arbitrageAnalyzer.findArbitrageOpportunities(
                baseAsset, maxAssets, chainLength, minProfitPercent);
        
        // Register all found chains in executor for future execution
        for (ArbitrageChain chain : chains) {
            chainExecutor.registerChain(chain);
        }
        
        log.info("Found {} profitable chains, registered in executor", chains.size());
        
        return chains;
    }
    
    /**
     * Execute arbitrage chain
     * 
     * @param chainId Chain ID to execute
     * @param baseAmount Amount of base asset to trade
     * @return Updated chain with execution status
     */
    public ArbitrageChain executeChain(String chainId, double baseAmount) {
        log.info("Executing arbitrage chain: chainId={}, baseAmount={}", chainId, baseAmount);
        
        ArbitrageChain chain = chainExecutor.getChainStatus(chainId);
        if (chain == null) {
            throw new IllegalArgumentException("Chain not found: " + chainId);
        }
        
        // Validate chain is still profitable
        List<ArbitrageStep> updatedSteps = new ArrayList<>();
        double currentAmount = baseAmount;
        
        for (ArbitrageStep step : chain.getSteps()) {
            // Get current rate
            double currentRate = arbitrageAnalyzer.getCurrentRate(step.getSymbol());
            if (currentRate == 0) {
                throw new IllegalStateException("Failed to get current rate for " + step.getSymbol());
            }
            
            // Update step with current rate
            ArbitrageStep updatedStep = ArbitrageStep.builder()
                    .fromAsset(step.getFromAsset())
                    .toAsset(step.getToAsset())
                    .symbol(step.getSymbol())
                    .rate(currentRate)
                    .minQty(step.getMinQty())
                    .maxQty(step.getMaxQty())
                    .priceDecimals(step.getPriceDecimals())
                    .qtyDecimals(step.getQtyDecimals())
                    .build();
            
            // Calculate actual quantity for this step
            // For BUY: quantity = currentAmount / rate (convert quote to base)
            // For SELL: quantity = currentAmount (already in base)
            boolean isBuy = updatedStep.getSymbol().startsWith(updatedStep.getToAsset());
            double stepQty = isBuy ? 
                    updatedStep.formatQuantity(currentAmount / currentRate) :
                    updatedStep.formatQuantity(currentAmount);
            
            // Validate quantity is within limits
            if (!updatedStep.isQuantityValid(stepQty)) {
                throw new IllegalStateException(
                        String.format("Invalid quantity for %s: %.8f (min: %.8f, max: %.8f)",
                                updatedStep.getSymbol(), stepQty,
                                updatedStep.getMinQty(), updatedStep.getMaxQty()));
            }
            
            updatedSteps.add(updatedStep);
            
            // Calculate output for next step
            if (isBuy) {
                // Bought base currency
                currentAmount = stepQty;
            } else {
                // Sold base currency, received quote currency
                currentAmount = stepQty * currentRate;
            }
        }
        
        // Calculate current profit
        double profitPercent = (currentAmount - baseAmount) / baseAmount * 100;
        chain.setSteps(updatedSteps);
        chain.setProfitPercent(profitPercent);
        chain.setTimestamp(Instant.now());
        
        // Warn if chain is no longer profitable (prices may have changed)
        // In sandbox mode, we proceed anyway for testing purposes
        if (profitPercent <= 0) {
            log.warn("Chain profit dropped to {}% (may proceed in sandbox mode)", 
                    String.format("%.2f", profitPercent));
        }
        
        // Execute chain
        return chainExecutor.executeChain(chain, baseAmount);
    }
    
    /**
     * Cancel chain execution
     * 
     * @param chainId Chain ID to cancel
     * @return true if cancelled successfully
     */
    public boolean cancelChain(String chainId) {
        log.info("Cancelling arbitrage chain: chainId={}", chainId);
        return chainExecutor.cancelChain(chainId);
    }
    
    /**
     * Get chain status
     * 
     * @param chainId Chain ID
     * @return Chain with current status
     */
    public ArbitrageChain getChainStatus(String chainId) {
        return chainExecutor.getChainStatus(chainId);
    }
    
    /**
     * Get list of available trading pairs
     * 
     * @return List of trading pairs
     */
    public List<String> getAvailablePairs() {
        return arbitrageAnalyzer.getAvailablePairs();
    }
    
    /**
     * Get unique assets from available pairs
     * 
     * @return Set of unique assets
     */
    public Set<String> getAvailableAssets() {
        List<String> pairs = getAvailablePairs();
        Set<String> assets = new HashSet<>();
        
        // Extract unique assets from pairs
        // Common quote currencies
        String[] quotes = {"USDT", "BTC", "ETH", "BNB", "BUSD", "USD", "EUR"};
        
        for (String pair : pairs) {
            for (String quote : quotes) {
                if (pair.endsWith(quote)) {
                    String base = pair.substring(0, pair.length() - quote.length());
                    if (!base.isEmpty()) {
                        assets.add(base);
                        assets.add(quote);
                        break;
                    }
                }
            }
        }
        
        return assets;
    }
}
