package com.nebulamind.tradingcore.domain.port;

import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;

/**
 * Port interface for executing arbitrage chains
 */
public interface ChainExecutor {
    
    /**
     * Execute arbitrage chain
     * 
     * @param chain Chain to execute
     * @param baseAmount Amount of base asset to trade
     * @return Updated chain with execution status
     */
    ArbitrageChain executeChain(ArbitrageChain chain, double baseAmount);
    
    /**
     * Cancel chain execution
     * 
     * @param chainId Chain ID to cancel
     * @return true if cancelled successfully
     */
    boolean cancelChain(String chainId);
    
    /**
     * Get chain execution status
     * 
     * @param chainId Chain ID
     * @return Chain with current status
     */
    ArbitrageChain getChainStatus(String chainId);
}
