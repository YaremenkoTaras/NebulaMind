package com.nebulamind.tradingcore.domain.port;

import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;

import java.util.List;

/**
 * Port interface for arbitrage analysis
 */
public interface ArbitrageAnalyzer {
    
    /**
     * Find profitable arbitrage opportunities
     * 
     * @param baseAsset Base asset (e.g. USDT)
     * @param maxAssets Maximum number of assets to analyze
     * @param chainLength Required length of arbitrage chain
     * @param minProfitPercent Minimum profit to consider
     * @return List of profitable chains, sorted by profit desc
     */
    List<ArbitrageChain> findArbitrageOpportunities(
            String baseAsset,
            int maxAssets,
            int chainLength,
            double minProfitPercent
    );
    
    /**
     * Get list of available trading pairs
     * 
     * @return List of trading pairs (e.g. BTCUSDT)
     */
    List<String> getAvailablePairs();
    
    /**
     * Get current exchange rate for pair
     * 
     * @param symbol Trading pair
     * @return Current rate or 0 if not available
     */
    double getCurrentRate(String symbol);
    
    /**
     * Check if trading pair is active
     * 
     * @param symbol Trading pair
     * @return true if pair is available for trading
     */
    boolean isPairActive(String symbol);
}
