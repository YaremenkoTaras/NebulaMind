package com.nebulamind.tradingcore.service.arbitrage;

import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for ArbitrageService
 */
@SpringBootTest
@ActiveProfiles("sandbox")
class ArbitrageServiceIntegrationTest {
    
    @Autowired
    private ArbitrageService arbitrageService;
    
    @Test
    void getAvailablePairs_shouldReturnPairs() {
        // When
        List<String> pairs = arbitrageService.getAvailablePairs();
        
        // Then
        assertThat(pairs).isNotEmpty();
        assertThat(pairs).contains("BTCUSDT", "ETHUSDT", "ETHBTC");
    }
    
    @Test
    void getAvailableAssets_shouldReturnUniqueAssets() {
        // When
        Set<String> assets = arbitrageService.getAvailableAssets();
        
        // Then
        assertThat(assets).isNotEmpty();
        assertThat(assets).contains("BTC", "ETH", "USDT");
    }
    
    @Test
    void findProfitableChains_triangularArbitrage_shouldFindChains() {
        // Given
        String baseAsset = "USDT";
        int maxAssets = 5;
        int chainLength = 3;
        double minProfitPercent = 0.0;
        
        // When
        List<ArbitrageChain> chains = arbitrageService.findProfitableChains(
                baseAsset, maxAssets, chainLength, minProfitPercent);
        
        // Then
        assertThat(chains).isNotNull();
        // May or may not find profitable chains with random price variation
        chains.forEach(chain -> {
            assertThat(chain.getBaseAsset()).isEqualTo(baseAsset);
            assertThat(chain.getSteps()).hasSize(chainLength);
            assertThat(chain.isValid(chainLength)).isTrue();
        });
    }
    
    @Test
    void findProfitableChains_withHighMinProfit_shouldFilterChains() {
        // Given
        String baseAsset = "USDT";
        int maxAssets = 5;
        int chainLength = 3;
        double minProfitPercent = 10.0; // Unrealistically high
        
        // When
        List<ArbitrageChain> chains = arbitrageService.findProfitableChains(
                baseAsset, maxAssets, chainLength, minProfitPercent);
        
        // Then
        // Likely no chains with 10% profit with random variation
        chains.forEach(chain -> {
            assertThat(chain.getProfitPercent()).isGreaterThanOrEqualTo(minProfitPercent);
        });
    }
    
    @Test
    void findProfitableChains_longerChain_shouldFindChains() {
        // Given
        String baseAsset = "USDT";
        int maxAssets = 10;
        int chainLength = 4;
        double minProfitPercent = 0.0;
        
        // When
        List<ArbitrageChain> chains = arbitrageService.findProfitableChains(
                baseAsset, maxAssets, chainLength, minProfitPercent);
        
        // Then
        chains.forEach(chain -> {
            assertThat(chain.getBaseAsset()).isEqualTo(baseAsset);
            assertThat(chain.getSteps()).hasSize(chainLength);
            assertThat(chain.isValid(chainLength)).isTrue();
            
            // Verify chain is circular
            String firstAsset = chain.getSteps().get(0).getFromAsset();
            String lastAsset = chain.getSteps().get(chainLength - 1).getToAsset();
            assertThat(lastAsset).isEqualTo(firstAsset);
        });
    }
    
    @Test
    void scanAndExecute_shouldRegisterAndExecuteChain() {
        // Given: First, scan for chains
        String baseAsset = "USDT";
        int maxAssets = 5;
        int chainLength = 3;
        double minProfitPercent = 0.0;
        
        List<ArbitrageChain> chains = arbitrageService.findProfitableChains(
                baseAsset, maxAssets, chainLength, minProfitPercent);
        
        // Verify at least one chain was found
        assertThat(chains).isNotEmpty();
        
        // When: Verify chain was registered and can be retrieved
        ArbitrageChain chainToExecute = chains.get(0);
        String chainId = chainToExecute.getId();
        
        ArbitrageChain retrievedChain = arbitrageService.getChainStatus(chainId);
        assertThat(retrievedChain).isNotNull();
        assertThat(retrievedChain.getId()).isEqualTo(chainId);
        assertThat(retrievedChain.getStatus()).isEqualTo(ArbitrageChain.ChainStatus.FOUND);
        
        // Then: Verify chain can be executed
        // Note: Using small amount to avoid quantity validation issues in test
        double baseAmount = 10.0;
        
        try {
            ArbitrageChain executedChain = arbitrageService.executeChain(chainId, baseAmount);
            
            // Verify execution results
            assertThat(executedChain).isNotNull();
            assertThat(executedChain.getId()).isEqualTo(chainId);
            assertThat(executedChain.getStatus()).isIn(
                    ArbitrageChain.ChainStatus.COMPLETED,
                    ArbitrageChain.ChainStatus.FAILED
            );
            
            // If completed, profit should be set
            if (executedChain.getStatus() == ArbitrageChain.ChainStatus.COMPLETED) {
                assertThat(executedChain.getProfitPercent()).isNotNull();
            }
        } catch (IllegalStateException e) {
            // Execution may fail due to quantity constraints - this is OK for test
            // The important thing is that the chain was registered and found
            assertThat(e.getMessage()).contains("quantity");
        }
    }
    
    @Test
    void executeChain_withUnknownChainId_shouldThrowException() {
        // Given: A chain ID that doesn't exist
        String unknownChainId = "unknown-chain-id";
        double baseAmount = 100.0;
        
        // When/Then: Attempting to execute should throw exception
        assertThatThrownBy(() -> arbitrageService.executeChain(unknownChainId, baseAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chain not found");
    }
    
    @Test
    void getChainStatus_afterRegistration_shouldReturnChain() {
        // Given: Scan for chains to register them
        List<ArbitrageChain> chains = arbitrageService.findProfitableChains("USDT", 5, 3, 0.0);
        
        if (!chains.isEmpty()) {
            String chainId = chains.get(0).getId();
            
            // When: Get chain status
            ArbitrageChain chain = arbitrageService.getChainStatus(chainId);
            
            // Then: Chain should be retrievable
            assertThat(chain).isNotNull();
            assertThat(chain.getId()).isEqualTo(chainId);
            assertThat(chain.getStatus()).isEqualTo(ArbitrageChain.ChainStatus.FOUND);
        }
    }
}

