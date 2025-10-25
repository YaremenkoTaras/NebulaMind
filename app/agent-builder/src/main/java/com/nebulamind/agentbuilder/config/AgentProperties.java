package com.nebulamind.agentbuilder.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Agent Builder
 */
@Data
@Validated
@ConfigurationProperties(prefix = "nebulamind.agent")
public class AgentProperties {

    @NotBlank
    private String name = "TradingAgent";
    
    private Llm llm = new Llm();
    private CoreApi coreApi = new CoreApi();
    private Audit audit = new Audit();

    @Data
    public static class Llm {
        @NotBlank
        private String provider = "openai";
        
        @NotBlank
        private String apiKey;
        
        @NotBlank
        private String model = "gpt-4";
        
        @Positive
        private int maxTokens = 2000;
        
        private double temperature = 0.7;
    }

    @Data
    public static class CoreApi {
        @NotBlank
        private String baseUrl = "http://localhost:8081";
        
        @Positive
        private int timeoutSeconds = 30;
    }

    @Data
    public static class Audit {
        @NotBlank
        private String logPath = "./logs/llm-trades.log";
    }
}

