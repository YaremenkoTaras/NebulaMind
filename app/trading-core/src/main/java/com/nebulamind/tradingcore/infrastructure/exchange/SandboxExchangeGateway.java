package com.nebulamind.tradingcore.infrastructure.exchange;

import com.nebulamind.tradingcore.config.NebulaMindProperties;
import com.nebulamind.tradingcore.domain.model.Order;
import com.nebulamind.tradingcore.domain.model.Portfolio;
import com.nebulamind.tradingcore.domain.model.Position;
import com.nebulamind.tradingcore.domain.port.ExchangeGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sandbox implementation of ExchangeGateway
 * 
 * Simulates exchange operations in-memory without real API calls
 */
@Component
@ConditionalOnProperty(name = "nebulamind.exchange.type", havingValue = "sandbox")
@RequiredArgsConstructor
@Slf4j
public class SandboxExchangeGateway implements ExchangeGateway {

    private final NebulaMindProperties properties;
    
    // In-memory storage
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private Portfolio portfolio;
    
    // Simulated prices (would be replaced with mock market data)
    private final Map<String, Double> prices = initializePrices();
    
    @jakarta.annotation.PostConstruct
    public void init() {
        portfolio = initializePortfolio();
    }
    
    private Map<String, Double> initializePrices() {
        Map<String, Double> priceMap = new ConcurrentHashMap<>();
        
        // USDT pairs
        priceMap.put("BTCUSDT", 50000.0);
        priceMap.put("ETHUSDT", 3000.0);
        priceMap.put("BNBUSDT", 400.0);
        priceMap.put("ADAUSDT", 0.5);
        priceMap.put("DOGEUSDT", 0.1);
        priceMap.put("XRPUSDT", 0.6);
        priceMap.put("SOLUSDT", 100.0);
        priceMap.put("DOTUSDT", 7.0);
        priceMap.put("MATICUSDT", 0.8);
        priceMap.put("LTCUSDT", 90.0);
        
        // BTC pairs
        priceMap.put("ETHBTC", 0.06);      // ETH/BTC
        priceMap.put("BNBBTC", 0.008);     // BNB/BTC
        priceMap.put("ADABTC", 0.00001);   // ADA/BTC
        priceMap.put("DOGEBTC", 0.000002); // DOGE/BTC
        priceMap.put("XRPBTC", 0.000012);  // XRP/BTC
        priceMap.put("SOLBTC", 0.002);     // SOL/BTC
        priceMap.put("DOTBTC", 0.00014);   // DOT/BTC
        priceMap.put("MATICBTC", 0.000016);// MATIC/BTC
        priceMap.put("LTCBTC", 0.0018);    // LTC/BTC
        
        // ETH pairs
        priceMap.put("BNBETH", 0.133);     // BNB/ETH
        priceMap.put("ADAETH", 0.000167);  // ADA/ETH
        priceMap.put("DOGEETH", 0.000033); // DOGE/ETH
        priceMap.put("XRPETH", 0.0002);    // XRP/ETH
        priceMap.put("SOLETH", 0.0333);    // SOL/ETH
        priceMap.put("DOTETH", 0.00233);   // DOT/ETH
        priceMap.put("MATICETH", 0.000267);// MATIC/ETH
        priceMap.put("LTCETH", 0.03);      // LTC/ETH
        
        return priceMap;
    }

    private Portfolio initializePortfolio() {
        log.info("Initializing sandbox portfolio with balance: {}", 
                properties.getSandbox().getInitialBalance());
        
        return Portfolio.builder()
                .accountId("SANDBOX_ACCOUNT")
                .freeBalance(properties.getSandbox().getInitialBalance())
                .lockedBalance(0.0)
                .totalEquity(properties.getSandbox().getInitialBalance())
                .currency("USDT")
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Order placeOrder(Order order) {
        log.info("Sandbox: Placing order {} {} {} @ {}", 
                order.getSide(), order.getQuantity(), order.getSymbol(), 
                order.getPrice() != null ? order.getPrice() : "MARKET");
        
        // Generate IDs
        if (order.getClientOrderId() == null) {
            order.setClientOrderId("SANDBOX_" + UUID.randomUUID().toString().substring(0, 8));
        }
        order.setOrderId("ORDER_" + System.currentTimeMillis());
        
        // Get current price
        double currentPrice = getCurrentPrice(order.getSymbol());
        double executionPrice = order.getPrice() != null ? order.getPrice() : currentPrice;
        
        // Execute order immediately in sandbox
        order.setStatus(Order.OrderStatus.FILLED);
        order.setExecutedQty(order.getQuantity());
        order.setAvgPrice(executionPrice);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        
        // Update portfolio
        updatePortfolioAfterTrade(order, executionPrice);
        
        // Store order
        orders.put(order.getClientOrderId(), order);
        
        log.info("Sandbox: Order {} executed at price {}", order.getClientOrderId(), executionPrice);
        return order;
    }

    @Override
    public Order cancelOrder(String clientOrderId) {
        log.info("Sandbox: Canceling order {}", clientOrderId);
        
        Order order = orders.get(clientOrderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + clientOrderId);
        }
        
        if (order.getStatus() == Order.OrderStatus.FILLED) {
            throw new IllegalStateException("Cannot cancel filled order");
        }
        
        order.setStatus(Order.OrderStatus.CANCELED);
        order.setUpdatedAt(Instant.now());
        
        return order;
    }

    @Override
    public Order getOrder(String clientOrderId) {
        return orders.get(clientOrderId);
    }

    @Override
    public Portfolio getPortfolio() {
        // Update positions with current prices
        portfolio.getPositions().forEach(position -> {
            double currentPrice = getCurrentPrice(position.getSymbol());
            position.updateUnrealizedPnL(currentPrice);
        });
        portfolio.calculateEquity();
        
        return portfolio;
    }

    @Override
    public double getCurrentPrice(String symbol) {
        // Simulate price movement with small random variation
        double basePrice = prices.getOrDefault(symbol, 1000.0);
        double variation = (Math.random() - 0.5) * 0.01; // ±0.5% variation
        return basePrice * (1 + variation);
    }

    @Override
    public boolean isAvailable() {
        return true; // Sandbox is always available
    }
    
    @Override
    public java.util.List<String> getAvailablePairs() {
        return new java.util.ArrayList<>(prices.keySet());
    }
    
    @Override
    public boolean isPairActive(String symbol) {
        return prices.containsKey(symbol);
    }

    /**
     * Update portfolio after trade execution
     */
    private void updatePortfolioAfterTrade(Order order, double price) {
        double tradeValue = order.getQuantity() * price;
        
        if (order.getSide() == Order.OrderSide.BUY) {
            // Deduct from balance
            portfolio.setFreeBalance(portfolio.getFreeBalance() - tradeValue);
            
            // Add or update position
            Position position = portfolio.getPosition(order.getSymbol());
            if (position == null) {
                position = Position.builder()
                        .symbol(order.getSymbol())
                        .side(Position.PositionSide.LONG)
                        .quantity(order.getQuantity())
                        .entryPrice(price)
                        .currentPrice(price)
                        .unrealizedPnL(0.0)
                        .realizedPnL(0.0)
                        .openedAt(Instant.now())
                        .updatedAt(Instant.now())
                        .stopLoss(order.getStopLossPrice())
                        .takeProfit(order.getTakeProfitPrice())
                        .build();
                portfolio.updatePosition(position);
            } else {
                // Average entry price
                double totalQty = position.getQuantity() + order.getQuantity();
                double avgEntry = ((position.getEntryPrice() * position.getQuantity()) + 
                                  (price * order.getQuantity())) / totalQty;
                position.setQuantity(totalQty);
                position.setEntryPrice(avgEntry);
                position.setUpdatedAt(Instant.now());
            }
        } else { // SELL
            // Add to balance
            portfolio.setFreeBalance(portfolio.getFreeBalance() + tradeValue);
            
            // Close or reduce position
            Position position = portfolio.getPosition(order.getSymbol());
            if (position != null) {
                if (position.getQuantity() <= order.getQuantity()) {
                    // Close position
                    double pnl = (price - position.getEntryPrice()) * position.getQuantity();
                    portfolio.removePosition(order.getSymbol());
                    log.info("Position closed with P&L: {}", pnl);
                } else {
                    // Reduce position
                    position.setQuantity(position.getQuantity() - order.getQuantity());
                    position.setUpdatedAt(Instant.now());
                }
            }
        }
        
        portfolio.calculateEquity();
    }

    /**
     * Update simulated price (for testing)
     */
    public void setPrice(String symbol, double price) {
        prices.put(symbol, price);
    }

    /**
     * Reset sandbox state
     */
    public void reset() {
        orders.clear();
        portfolio.getPositions().clear();
        portfolio.setFreeBalance(properties.getSandbox().getInitialBalance());
        portfolio.calculateEquity();
        log.info("Sandbox reset to initial state");
    }
}

