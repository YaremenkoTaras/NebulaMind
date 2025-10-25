package com.nebulamind.tradingcore.infrastructure.arbitrage;

import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;
import com.nebulamind.tradingcore.domain.port.ChainExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stub implementation of ChainExecutor for development
 * 
 * TODO: Implement real chain execution logic
 */
@Component
@Slf4j
public class StubChainExecutor implements ChainExecutor {
    
    private final Map<String, ArbitrageChain> chains = new ConcurrentHashMap<>();
    
    @Override
    public ArbitrageChain executeChain(ArbitrageChain chain, double baseAmount) {
        log.warn("StubChainExecutor.executeChain called - marking as FAILED");
        log.info("Chain ID: {}, Base Amount: {}", chain.getId(), baseAmount);
        
        // TODO: Implement real execution
        // For now, just mark as failed
        chain.setStatus(ArbitrageChain.ChainStatus.FAILED);
        chains.put(chain.getId(), chain);
        
        return chain;
    }
    
    @Override
    public boolean cancelChain(String chainId) {
        log.warn("StubChainExecutor.cancelChain called for {}", chainId);
        
        ArbitrageChain chain = chains.get(chainId);
        if (chain != null) {
            chain.setStatus(ArbitrageChain.ChainStatus.CANCELLED);
            return true;
        }
        
        return false;
    }
    
    @Override
    public ArbitrageChain getChainStatus(String chainId) {
        log.debug("StubChainExecutor.getChainStatus called for {}", chainId);
        return chains.get(chainId);
    }
}

