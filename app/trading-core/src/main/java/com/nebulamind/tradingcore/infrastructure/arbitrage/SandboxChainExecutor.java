package com.nebulamind.tradingcore.infrastructure.arbitrage;

import com.nebulamind.tradingcore.domain.model.Order;
import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageChain;
import com.nebulamind.tradingcore.domain.model.arbitrage.ArbitrageStep;
import com.nebulamind.tradingcore.domain.port.ChainExecutor;
import com.nebulamind.tradingcore.domain.port.ExchangeGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of ChainExecutor for sandbox
 * 
 * Executes arbitrage chains by placing orders sequentially
 */
@Component
@ConditionalOnProperty(name = "nebulamind.exchange.type", havingValue = "sandbox")
@RequiredArgsConstructor
@Slf4j
public class SandboxChainExecutor implements ChainExecutor {
    
    private final ExchangeGateway exchangeGateway;
    private final Map<String, ArbitrageChain> chains = new ConcurrentHashMap<>();
    private final Map<String, ExecutionContext> executionContexts = new ConcurrentHashMap<>();
    
    @Override
    public ArbitrageChain executeChain(ArbitrageChain chain, double baseAmount) {
        log.info("Executing arbitrage chain: {} with base amount: {}", chain.getId(), baseAmount);
        
        // Store chain
        chains.put(chain.getId(), chain);
        chain.setStatus(ArbitrageChain.ChainStatus.EXECUTING);
        
        // Create execution context
        ExecutionContext context = new ExecutionContext();
        context.chainId = chain.getId();
        context.startAmount = baseAmount;
        context.currentAmount = baseAmount;
        context.executedOrders = new ArrayList<>();
        context.startTime = Instant.now();
        executionContexts.put(chain.getId(), context);
        
        try {
            // Execute each step in the chain
            for (int i = 0; i < chain.getSteps().size(); i++) {
                ArbitrageStep step = chain.getSteps().get(i);
                log.info("Executing step {}/{}: {} -> {} ({})", 
                        i + 1, chain.getSteps().size(), 
                        step.getFromAsset(), step.getToAsset(), step.getSymbol());
                
                // Place order for this step
                Order order = createOrderForStep(step, context.currentAmount);
                Order executedOrder = exchangeGateway.placeOrder(order);
                
                // Check execution
                if (executedOrder.getStatus() != Order.OrderStatus.FILLED) {
                    throw new RuntimeException("Order not filled: " + executedOrder.getStatus());
                }
                
                // Update context
                context.executedOrders.add(executedOrder);
                context.currentAmount = calculateOutputAmount(executedOrder, step);
                
                log.info("Step executed: received {} {}", context.currentAmount, step.getToAsset());
            }
            
            // Calculate final profit
            double profit = context.currentAmount - context.startAmount;
            double profitPercent = (profit / context.startAmount) * 100.0;
            
            chain.setStatus(ArbitrageChain.ChainStatus.COMPLETED);
            chain.setProfitPercent(profitPercent);
            
            log.info("Chain execution completed: start={}, end={}, profit={}%", 
                    context.startAmount, context.currentAmount, profitPercent);
            
        } catch (Exception e) {
            log.error("Chain execution failed: {}", e.getMessage(), e);
            chain.setStatus(ArbitrageChain.ChainStatus.FAILED);
            
            // Try to rollback (cancel pending orders)
            rollbackExecution(context);
        }
        
        return chain;
    }
    
    @Override
    public boolean cancelChain(String chainId) {
        log.info("Cancelling arbitrage chain: {}", chainId);
        
        ArbitrageChain chain = chains.get(chainId);
        if (chain == null) {
            log.warn("Chain not found: {}", chainId);
            return false;
        }
        
        // Only can cancel if not executing
        if (chain.getStatus() == ArbitrageChain.ChainStatus.EXECUTING) {
            log.warn("Cannot cancel executing chain: {}", chainId);
            return false;
        }
        
        chain.setStatus(ArbitrageChain.ChainStatus.CANCELLED);
        return true;
    }
    
    @Override
    public ArbitrageChain getChainStatus(String chainId) {
        return chains.get(chainId);
    }
    
    /**
     * Create order for arbitrage step
     */
    private Order createOrderForStep(ArbitrageStep step, double currentAmount) {
        // Determine order side and quantity
        Order.OrderSide side = determineSide(step);
        double quantity = calculateQuantity(step, currentAmount);
        
        return Order.builder()
                .symbol(step.getSymbol())
                .side(side)
                .type(Order.OrderType.MARKET)
                .quantity(quantity)
                .build();
    }
    
    /**
     * Determine order side (BUY or SELL)
     */
    private Order.OrderSide determineSide(ArbitrageStep step) {
        // If symbol ends with fromAsset, we're selling fromAsset
        // Otherwise, we're buying toAsset with fromAsset
        if (step.getSymbol().startsWith(step.getToAsset())) {
            return Order.OrderSide.BUY;
        } else {
            return Order.OrderSide.SELL;
        }
    }
    
    /**
     * Calculate order quantity
     */
    private double calculateQuantity(ArbitrageStep step, double currentAmount) {
        // For now, use current amount directly
        // In real implementation, consider min/max quantities and lot sizes
        double quantity = currentAmount;
        
        // Format to step precision
        return step.formatQuantity(quantity);
    }
    
    /**
     * Calculate output amount after order execution
     */
    private double calculateOutputAmount(Order executedOrder, ArbitrageStep step) {
        if (executedOrder.getSide() == Order.OrderSide.BUY) {
            // Bought toAsset
            return executedOrder.getExecutedQty();
        } else {
            // Sold fromAsset, received quote currency
            return executedOrder.getExecutedQty() * executedOrder.getAvgPrice();
        }
    }
    
    /**
     * Rollback execution on failure
     */
    private void rollbackExecution(ExecutionContext context) {
        log.warn("Rolling back execution for chain: {}", context.chainId);
        
        // In sandbox, orders are executed immediately, so rollback is not really possible
        // In real implementation, we would cancel pending orders
        // and possibly place reverse orders to unwind positions
        
        log.warn("Rollback not implemented for sandbox");
    }
    
    /**
     * Execution context
     */
    private static class ExecutionContext {
        String chainId;
        double startAmount;
        double currentAmount;
        List<Order> executedOrders;
        Instant startTime;
    }
}

