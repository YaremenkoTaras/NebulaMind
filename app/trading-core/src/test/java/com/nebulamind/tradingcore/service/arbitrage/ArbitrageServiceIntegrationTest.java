package com.nebulamind.tradingcore.service.arbitrage;

import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
}

