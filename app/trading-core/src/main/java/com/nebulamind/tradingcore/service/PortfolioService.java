package com.nebulamind.tradingcore.service;

import com.nebulamind.tradingcore.api.dto.PortfolioDto;
import com.nebulamind.tradingcore.api.dto.PositionDto;
import com.nebulamind.tradingcore.domain.model.Portfolio;
import com.nebulamind.tradingcore.domain.model.Position;
import com.nebulamind.tradingcore.domain.port.ExchangeGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service for portfolio operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final ExchangeGateway exchangeGateway;

    /**
     * Get current portfolio snapshot
     * 
     * @return Portfolio with equity, positions, and balance
     */
    public PortfolioDto getPortfolio() {
        log.debug("Getting portfolio snapshot");
        
        Portfolio portfolio = exchangeGateway.getPortfolio();
        
        return PortfolioDto.builder()
                .timestamp(portfolio.getTimestamp())
                .totalEquity(portfolio.getTotalEquity())
                .freeBalance(portfolio.getFreeBalance())
                .lockedBalance(portfolio.getLockedBalance())
                .positions(portfolio.getPositions().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList()))
                .currency(portfolio.getCurrency())
                .build();
    }
    
    private PositionDto toDto(Position position) {
        return PositionDto.builder()
                .symbol(position.getSymbol())
                .quantity(position.getQuantity())
                .entryPrice(position.getEntryPrice())
                .currentPrice(position.getCurrentPrice())
                .unrealizedPnL(position.getUnrealizedPnL())
                .side(position.getSide().name())
                .build();
    }
}

