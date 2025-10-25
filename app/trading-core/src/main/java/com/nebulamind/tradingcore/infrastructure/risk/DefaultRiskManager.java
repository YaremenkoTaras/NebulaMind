package com.nebulamind.tradingcore.infrastructure.risk;

import com.nebulamind.tradingcore.config.NebulaMindProperties;
import com.nebulamind.tradingcore.domain.model.Order;
import com.nebulamind.tradingcore.domain.model.Portfolio;
import com.nebulamind.tradingcore.domain.port.RiskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of RiskManager
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultRiskManager implements RiskManager {

    private final NebulaMindProperties properties;
    
    // Track daily P&L
    private final Map<LocalDate, Double> dailyPnL = new ConcurrentHashMap<>();

    @Override
    public ValidationResult validateOrder(Order order, Portfolio portfolio) {
        log.debug("Validating order: {} {} {}", order.getSide(), order.getQuantity(), order.getSymbol());
        
        // Check stop loss presence
        if (order.getStopLossPrice() == null) {
            return ValidationResult.fail("Stop loss is mandatory");
        }
        
        // Check daily loss limit
        if (isDailyLossLimitExceeded()) {
            return ValidationResult.fail("Daily loss limit exceeded");
        }
        
        // Calculate order value
        double orderValue = order.getQuantity() * (order.getPrice() != null ? order.getPrice() : 50000.0);
        double maxOrderValue = portfolio.getTotalEquity() * (properties.getRisk().getMaxPctEquity() / 100.0);
        
        if (orderValue > maxOrderValue) {
            return ValidationResult.fail(
                    String.format("Order value %.2f exceeds max allowed %.2f (%.1f%% of equity)", 
                            orderValue, maxOrderValue, properties.getRisk().getMaxPctEquity()));
        }
        
        // Check available balance for BUY orders
        if (order.getSide() == Order.OrderSide.BUY) {
            if (orderValue > portfolio.getFreeBalance()) {
                return ValidationResult.fail(
                        String.format("Insufficient balance: required %.2f, available %.2f", 
                                orderValue, portfolio.getFreeBalance()));
            }
        }
        
        // Validate stop loss percentage
        if (order.getPrice() != null && order.getStopLossPrice() != null) {
            double stopLossPct = Math.abs((order.getPrice() - order.getStopLossPrice()) / order.getPrice() * 100);
            if (stopLossPct > properties.getRisk().getStopLossPct() * 2) {
                log.warn("Stop loss {}% exceeds recommended {}%", stopLossPct, properties.getRisk().getStopLossPct());
            }
        }
        
        log.debug("Order validation passed");
        return ValidationResult.ok();
    }

    @Override
    public boolean isDailyLossLimitExceeded() {
        LocalDate today = LocalDate.now();
        double todayPnL = dailyPnL.getOrDefault(today, 0.0);
        
        // Negative P&L means loss
        if (todayPnL < 0) {
            double maxDailyLoss = properties.getRisk().getDailyLossLimitPct() / 100.0;
            double lossRatio = Math.abs(todayPnL);
            
            if (lossRatio >= maxDailyLoss) {
                log.warn("Daily loss limit exceeded: {:.2f}% >= {:.2f}%", lossRatio * 100, maxDailyLoss * 100);
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void recordTrade(Order order, double pnl) {
        LocalDate today = LocalDate.now();
        dailyPnL.merge(today, pnl, Double::sum);
        
        log.info("Trade recorded: order={}, pnl={:.2f}, daily_total={:.2f}", 
                order.getClientOrderId(), pnl, dailyPnL.get(today));
    }

    /**
     * Reset daily P&L (for testing)
     */
    public void resetDailyPnL() {
        dailyPnL.clear();
        log.info("Daily P&L reset");
    }

    /**
     * Get current daily P&L
     */
    public double getDailyPnL() {
        return dailyPnL.getOrDefault(LocalDate.now(), 0.0);
    }
}

