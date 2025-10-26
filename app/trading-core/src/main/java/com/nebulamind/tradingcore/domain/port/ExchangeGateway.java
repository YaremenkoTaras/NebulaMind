package com.nebulamind.tradingcore.domain.port;

import com.nebulamind.tradingcore.domain.model.Order;
import com.nebulamind.tradingcore.domain.model.Portfolio;

/**
 * Port interface for exchange operations
 * 
 * Implementations:
 * - SandboxExchangeGateway: In-memory simulation
 * - BinanceExchangeGateway: Real Binance API integration
 */
public interface ExchangeGateway {
    
    /**
     * Place a new order on the exchange
     * 
     * @param order Order to place
     * @return Updated order with execution details
     */
    Order placeOrder(Order order);
    
    /**
     * Cancel an existing order
     * 
     * @param clientOrderId Client order ID
     * @return Updated order with canceled status
     */
    Order cancelOrder(String clientOrderId);
    
    /**
     * Get order by client order ID
     * 
     * @param clientOrderId Client order ID
     * @return Order or null if not found
     */
    Order getOrder(String clientOrderId);
    
    /**
     * Get current portfolio state
     * 
     * @return Portfolio with balances and positions
     */
    Portfolio getPortfolio();
    
    /**
     * Get current market price for a symbol
     * 
     * @param symbol Trading pair symbol
     * @return Current market price
     */
    double getCurrentPrice(String symbol);
    
    /**
     * Check if exchange is available
     * 
     * @return true if exchange is reachable
     */
    boolean isAvailable();
    
    /**
     * Get list of available trading pairs
     * 
     * @return List of trading pair symbols (e.g., ["BTCUSDT", "ETHUSDT"])
     */
    java.util.List<String> getAvailablePairs();
    
    /**
     * Check if a trading pair is active
     * 
     * @param symbol Trading pair symbol
     * @return true if pair is available for trading
     */
    boolean isPairActive(String symbol);
}

