package com.nebulamind.tradingcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * NebulaMind Trading Core Application
 * 
 * Main entry point for the trading engine. Provides:
 * - REST API for agent integration
 * - Exchange gateway (Binance/Sandbox)
 * - Risk management
 * - Order routing
 * - Market data processing
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class TradingCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingCoreApplication.class, args);
    }
}

