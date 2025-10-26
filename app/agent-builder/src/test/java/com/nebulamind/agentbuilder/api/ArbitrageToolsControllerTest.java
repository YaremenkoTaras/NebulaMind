package com.nebulamind.agentbuilder.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebulamind.agentbuilder.dto.ArbitrageScanRequest;
import com.nebulamind.agentbuilder.dto.ArbitrageScanResponse;
import com.nebulamind.agentbuilder.tool.ArbitrageTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for ArbitrageToolsController
 */
@WebMvcTest(ArbitrageToolsController.class)
class ArbitrageToolsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArbitrageTools arbitrageTools;

    @Test
    void testScanForArbitrage_Success() throws Exception {
        // Given
        ArbitrageScanRequest request = ArbitrageScanRequest.builder()
                .baseAsset("USDT")
                .maxAssets(10)
                .chainLength(3)
                .minProfitPercent(0.5)
                .reasoning("Testing arbitrage scan")
                .build();

        ArbitrageScanResponse.ArbitrageChainDto chain = ArbitrageScanResponse.ArbitrageChainDto.builder()
                .id("chain-123")
                .baseAsset("USDT")
                .steps(List.of("BTCUSDT", "ETHBTC", "ETHUSDT"))
                .profitPercent(1.25)
                .minRequiredAmount(100.0)
                .timestamp("2025-10-25T10:00:00Z")
                .build();

        ArbitrageScanResponse.ScanSummary summary = ArbitrageScanResponse.ScanSummary.builder()
                .totalChainsFound(1)
                .profitableChainsFound(1)
                .bestProfitPercent(1.25)
                .analyzedAssets(List.of("BTC", "ETH", "USDT"))
                .build();

        ArbitrageScanResponse response = ArbitrageScanResponse.builder()
                .chains(List.of(chain))
                .summary(summary)
                .build();

        when(arbitrageTools.scanForArbitrage(any(ArbitrageScanRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/agent/arbitrage/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chains").isArray())
                .andExpect(jsonPath("$.chains[0].id").value("chain-123"))
                .andExpect(jsonPath("$.chains[0].baseAsset").value("USDT"))
                .andExpect(jsonPath("$.chains[0].profitPercent").value(1.25))
                .andExpect(jsonPath("$.summary.totalChainsFound").value(1))
                .andExpect(jsonPath("$.summary.profitableChainsFound").value(1))
                .andExpect(jsonPath("$.summary.bestProfitPercent").value(1.25));
    }

    @Test
    void testScanForArbitrage_NoChainsFound() throws Exception {
        // Given
        ArbitrageScanRequest request = ArbitrageScanRequest.builder()
                .baseAsset("USDT")
                .maxAssets(5)
                .chainLength(3)
                .minProfitPercent(5.0)
                .build();

        ArbitrageScanResponse.ScanSummary summary = ArbitrageScanResponse.ScanSummary.builder()
                .totalChainsFound(0)
                .profitableChainsFound(0)
                .bestProfitPercent(0.0)
                .analyzedAssets(List.of("BTC", "ETH"))
                .build();

        ArbitrageScanResponse response = ArbitrageScanResponse.builder()
                .chains(List.of())
                .summary(summary)
                .build();

        when(arbitrageTools.scanForArbitrage(any(ArbitrageScanRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/agent/arbitrage/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chains").isEmpty())
                .andExpect(jsonPath("$.summary.totalChainsFound").value(0))
                .andExpect(jsonPath("$.summary.profitableChainsFound").value(0));
    }

    @Test
    void testGetAvailableAssets_Success() throws Exception {
        // Given
        List<String> assets = List.of("BTC", "ETH", "BNB", "SOL", "ADA", "XRP");

        when(arbitrageTools.getAvailableAssets()).thenReturn(assets);

        // When & Then
        mockMvc.perform(get("/api/agent/arbitrage/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0]").value("BTC"))
                .andExpect(jsonPath("$[1]").value("ETH"));
    }

    @Test
    void testGetAvailableAssets_EmptyList() throws Exception {
        // Given
        when(arbitrageTools.getAvailableAssets()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/agent/arbitrage/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testExecuteChain_Success() throws Exception {
        // Given
        String chainId = "chain-123";
        double baseAmount = 100.0;

        Map<String, Object> executionResult = Map.of(
                "chainId", chainId,
                "status", "COMPLETED",
                "initialAmount", baseAmount,
                "finalAmount", 101.25,
                "actualProfit", 1.25,
                "actualProfitPercent", 1.25,
                "steps", List.of(
                        Map.of("symbol", "BTCUSDT", "amount", 0.002, "status", "FILLED"),
                        Map.of("symbol", "ETHBTC", "amount", 0.05, "status", "FILLED"),
                        Map.of("symbol", "ETHUSDT", "amount", 101.25, "status", "FILLED")
                )
        );

        when(arbitrageTools.executeArbitrageChain(eq(chainId), eq(baseAmount))).thenReturn(executionResult);

        // When & Then
        mockMvc.perform(post("/api/agent/arbitrage/chains/{chainId}/execute", chainId)
                        .param("baseAmount", String.valueOf(baseAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chainId").value(chainId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.initialAmount").value(baseAmount))
                .andExpect(jsonPath("$.finalAmount").value(101.25))
                .andExpect(jsonPath("$.actualProfit").value(1.25));
    }

    @Test
    void testExecuteChain_InvalidAmount() throws Exception {
        // Given
        String chainId = "chain-123";
        double invalidAmount = -10.0;

        // When & Then
        mockMvc.perform(post("/api/agent/arbitrage/chains/{chainId}/execute", chainId)
                        .param("baseAmount", String.valueOf(invalidAmount)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testExecuteChain_ZeroAmount() throws Exception {
        // Given
        String chainId = "chain-123";
        double zeroAmount = 0.0;

        // When & Then
        mockMvc.perform(post("/api/agent/arbitrage/chains/{chainId}/execute", chainId)
                        .param("baseAmount", String.valueOf(zeroAmount)))
                .andExpect(status().isBadRequest());
    }
}

