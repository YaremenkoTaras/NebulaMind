package com.nebulamind.tradingcore.api;

import com.nebulamind.tradingcore.api.dto.PortfolioDto;
import com.nebulamind.tradingcore.service.OrderService;
import com.nebulamind.tradingcore.service.PortfolioService;
import com.nebulamind.tradingcore.service.SignalService;
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
 * Tests for CoreController
 */
@WebMvcTest(CoreController.class)
class CoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private SignalService signalService;

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

        when(portfolioService.getPortfolio()).thenReturn(portfolio);

        // When & Then
        mockMvc.perform(get("/api/core/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEquity").value(10000.0))
                .andExpect(jsonPath("$.currency").value("USDT"));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/core/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Trading Core is running"));
    }
}

