package com.nebulamind.tradingcore.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for NebulaMind Trading Core
 */
@Data
@Validated
@ConfigurationProperties(prefix = "nebulamind")
public class NebulaMindProperties {

    private Exchange exchange = new Exchange();
    private Risk risk = new Risk();
    private Sandbox sandbox = new Sandbox();

    @Data
    public static class Exchange {
        @NotBlank
        private String type = "sandbox"; // sandbox | binance
        
        @NotBlank
        private String name = "FakeBinance";
        
        private String apiKey;
        private String apiSecret;
        private String baseUrl = "https://testnet.binance.vision";
    }

    @Data
    public static class Risk {
        @Min(0)
        @Max(100)
        private double maxPctEquity = 5.0;
        
        @Min(0)
        @Max(100)
        private double stopLossPct = 2.0;
        
        @Min(0)
        @Max(100)
        private double dailyLossLimitPct = 10.0;
    }

    @Data
    public static class Sandbox {
        @Min(0)
        private double initialBalance = 10000.0;
        
        @Min(0)
        private double initialBtc = 0.0;
    }
}

