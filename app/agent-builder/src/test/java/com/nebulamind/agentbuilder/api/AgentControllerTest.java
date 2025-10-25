package com.nebulamind.agentbuilder.api;

import com.nebulamind.agentbuilder.dto.PortfolioDto;
import com.nebulamind.agentbuilder.tool.TradingTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for AgentController
 */
@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradingTools tradingTools;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/agent/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Agent Builder is running"));
    }

    @Test
    void testGetPortfolio() throws Exception {
        // Given
        PortfolioDto portfolio = PortfolioDto.builder()
                .timestamp(Instant.now())
                .totalEquity(10000.0)
                .freeBalance(10000.0)
                .lockedBalance(0.0)
                .positions(Collections.emptyList())
                .currency("USDT")
                .build();

        when(tradingTools.getPortfolio()).thenReturn(portfolio);

        // When & Then
        mockMvc.perform(get("/api/agent/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEquity").value(10000.0))
                .andExpect(jsonPath("$.currency").value("USDT"));
    }
}

